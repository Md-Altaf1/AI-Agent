package com.example.jobagent.resume.parser;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ResumeParser {
    String extractText(MultipartFile file) throws IOException;

}