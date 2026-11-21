package com.example.jobagent.resume.dto;

import java.nio.file.Path;

public record ResumeFileDownload(
        Path filePath,
        String originalFilename,
        String contentType
) {
}