package com.example.jobagent.resume.service;
import com.example.jobagent.resume.dto.ResumeFileDownload;
import com.example.jobagent.auth.entity.User;
import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.common.config.exception.ResourceNotFoundException;
import com.example.jobagent.resume.dto.ResumeUploadResponse;
import com.example.jobagent.resume.entity.Resume;
import com.example.jobagent.resume.entity.ResumeVersion;
import com.example.jobagent.resume.parser.ResumeParsingService;
import com.example.jobagent.resume.repository.ResumeRepository;
import com.example.jobagent.resume.repository.ResumeVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.example.jobagent.resume.entity.ParseStatus;
import com.example.jobagent.resume.dto.ResumeResponse;
import com.example.jobagent.resume.dto.ResumeVersionResponse;

import java.nio.file.Path;
import java.util.List;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import com.example.jobagent.resume.dto.ResumeResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ResumeService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".pdf", ".docx");

    private final ResumeParsingService resumeParsingService;
    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository versionRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ResumeService(
            ResumeParsingService resumeParsingService,
            ResumeRepository resumeRepository,
            ResumeVersionRepository versionRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService) {
        this.resumeParsingService = resumeParsingService;
        this.resumeRepository = resumeRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ResumeUploadResponse uploadResume(
            String email,
            String label,
            MultipartFile file) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (label == null || label.isBlank() || label.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Resume label must contain 1-100 characters"
            );
        }

        validateFile(file);

        String extractedText;

        try {
            extractedText = resumeParsingService.extractResumeText(file);
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Unable to read or parse the resume file",
                    exception
            );
        }
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String sha256 = calculateSha256(file);

        Resume resume = resumeRepository
                .findByUserIdAndLabelIgnoreCase(user.getId(), label.trim())
                .orElseGet(() ->
                        resumeRepository.save(
                                new Resume(user, label.trim())
                        ));

        boolean duplicate = versionRepository
                .findByResumeIdAndSha256(resume.getId(), sha256)
                .isPresent();

        if (duplicate) {
            throw new IllegalArgumentException(
                    "This resume file has already been uploaded"
            );
        }
        Integer maxVersion =
                versionRepository.findMaxVersionNoByResumeId(resume.getId());
        int versionNo = (maxVersion == null) ? 1 : maxVersion + 1;
        String storageKey = null;
        try {

            storageKey = fileStorageService.store(file, extension);

            ResumeVersion resumeVersion = new ResumeVersion(
                    resume,
                    versionNo,
                    originalFilename,
                    getContentType(extension),
                    Math.toIntExact(file.getSize()),
                    sha256,
                    storageKey,
                    extractedText
            );

// Mark parsing as successful
            resumeVersion.setParseStatus(ParseStatus.PARSED);

            ResumeVersion savedVersion = versionRepository.save(resumeVersion);
            return ResumeUploadResponse.fromEntity(savedVersion);

        } catch (RuntimeException exception) {

            if (storageKey != null) {
                try {
                    fileStorageService.delete(storageKey, extension);
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getAllResumes(String email) {

        // 1. Find authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Fetch user's resumes
        List<Resume> resumes =
                resumeRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());

        // 3. Return empty list if user has no resumes
        if (resumes.isEmpty()) {
            return List.of();
        }

        // 4. Extract resume IDs
        List<Long> resumeIds = resumes.stream()
                .map(Resume::getId)
                .toList();

        // 5. Fetch latest version numbers in one query
        Map<Long, Integer> latestVersions =
                versionRepository
                        .findLatestVersionNumbersByResumeIds(resumeIds)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> ((Number) row[1]).intValue()
                        ));

        // 6. Convert entities into response DTOs
        return resumes.stream()
                .map(resume -> {

                    ResumeResponse response =
                            ResumeResponse.fromEntity(resume);

                    response.setLatestVersionNo(
                            latestVersions.get(resume.getId())
                    );

                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResumeVersionResponse> getResumeVersions(
            String email,
            Long resumeId
    ) {

        // 1. Find authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Verify resume ownership
        Resume resume = resumeRepository
                .findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resume not found"));

        // 3. Fetch all versions, latest first
        List<ResumeVersion> versions =
                versionRepository.findByResumeIdOrderByVersionNoDesc(
                        resume.getId()
                );

        // 4. Convert entities into DTOs
        return versions.stream()
                .map(ResumeVersionResponse::fromEntity)
                .toList();
    }
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resume file is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Resume file size must not exceed 5 MB"
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid filename"
            );
        }

        String extension = getExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only PDF and DOCX files are allowed"
            );
        }

        try {
            byte[] bytes = file.getBytes();

            if (extension.equals(".pdf")) {

                if (bytes.length < 5 ||
                        !new String(bytes, 0, 5)
                                .equals("%PDF-")) {

                    throw new IllegalArgumentException(
                            "Invalid PDF file"
                    );
                }

            } else if (extension.equals(".docx")) {

                if (bytes.length < 4 ||
                        bytes[0] != 'P' ||
                        bytes[1] != 'K') {

                    throw new IllegalArgumentException(
                            "Invalid DOCX file"
                    );
                }
            }

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not read uploaded file",
                    exception
            );
        }
    }

    private String getExtension(String filename) {

        int lastDot = filename.lastIndexOf('.');

        if (lastDot < 0) {
            throw new IllegalArgumentException(
                    "File extension is missing"
            );
        }

        return filename.substring(lastDot)
                .toLowerCase(Locale.ROOT);
    }

    private String getContentType(String extension) {

        return switch (extension) {
            case ".pdf" -> "application/pdf";

            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            default -> throw new IllegalArgumentException(
                    "Unsupported file format"
            );
        };
    }

    private String calculateSha256(MultipartFile file) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(file.getBytes());

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new RuntimeException(
                    "Failed to calculate SHA-256",
                    exception
            );
        }
    }
    @Transactional(readOnly = true)
    public ResumeVersionResponse getResumeVersion(
            String email,
            Long resumeId,
            Long versionId
    ) {
        // 1. Find authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Verify resume ownership
        Resume resume = resumeRepository
                .findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resume not found"));

        // 3. Find version belonging to this resume
        ResumeVersion version = versionRepository
                .findByIdAndResumeId(versionId, resume.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resume version not found"));

        // 4. Convert entity to DTO
        return ResumeVersionResponse.fromEntity(version);
    }

    @Transactional(readOnly = true)
    public ResumeFileDownload downloadResumeVersion(
            String email,
            Long resumeId,
            Long versionId
    ) {

        // 1. Find authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Verify resume ownership
        Resume resume = resumeRepository
                .findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resume not found"));

        // 3. Find requested version
        ResumeVersion version = versionRepository
                .findByIdAndResumeId(versionId, resume.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resume version not found"));

        // 4. Get stored file path
        String extension = getExtension(version.getOriginalFilename());

        Path filePath = fileStorageService.load(
                version.getStorageKey(),
                extension
        );

        // 5. Return download information
        return new ResumeFileDownload(
                filePath,
                version.getOriginalFilename(),
                version.getContentType()
        );
    }


    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    @Transactional
    public void deleteResumeVersion(String email, Long resumeId, Long versionId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Resume resume = resumeRepository
                .findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        ResumeVersion version = versionRepository
                .findByIdAndResumeId(versionId, resume.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found"));

        String storageKey = version.getStorageKey();
        String extension = getExtension(version.getOriginalFilename());

        versionRepository.delete(version);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            fileStorageService.delete(storageKey, extension);
                        } catch (RuntimeException exception) {
                            log.error("Failed to delete physical resume file: {}", storageKey, exception);
                        }
                    }
                }
        );
    }

}