package com.example.jobagent.resume.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class DocxResumeParser implements ResumeParser {
    @Override
    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("DOCX file is required");
        }
        try (XWPFDocument document =
                     new XWPFDocument(file.getInputStream())) {
            String extractedText = document.getParagraphs()
                    .stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
            if (extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text found in DOCX"
                );
            }
            return extractedText.trim();
        }
    }
}