package com.example.jobagent.resume.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PdfResumeParser implements ResumeParser {
    @Override
    public String extractText(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        try (PDDocument document =
                     Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException(
                        "Encrypted PDF files are not supported"
                );
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);
            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text found in PDF"
                );
            }
            return extractedText.trim();
        }
    }
}