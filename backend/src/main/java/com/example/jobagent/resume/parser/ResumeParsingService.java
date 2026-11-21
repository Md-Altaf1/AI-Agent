package com.example.jobagent.resume.parser;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
public class ResumeParsingService {
    private final PdfResumeParser pdfResumeParser;
    private final DocxResumeParser docxResumeParser;
    public ResumeParsingService(
            PdfResumeParser pdfResumeParser,
            DocxResumeParser docxResumeParser) {
        this.pdfResumeParser = pdfResumeParser;
        this.docxResumeParser = docxResumeParser;
    }

    public String extractResumeText(MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resume file is required"
            );
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid resume filename"
            );
        }
        String lowerCaseFilename =
                filename.toLowerCase(Locale.ROOT);
        if (lowerCaseFilename.endsWith(".pdf")) {
            return pdfResumeParser.extractText(file);
        }
        if (lowerCaseFilename.endsWith(".docx")) {
            return docxResumeParser.extractText(file);
        }
        throw new IllegalArgumentException(
                "Only PDF and DOCX files are supported"
        );
    }
}