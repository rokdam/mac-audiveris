package com.example.akbo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/audiveris")
public class AudiverisController {

    private static final String AUDIVERIS_PATH = "../audiveris/build/libs"; // Audiveris 설치 경로

    @PostMapping("/process")
    public ResponseEntity<String> processScore(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file provided");
        }

        try {
            // 파일 저장
            String filename = file.getOriginalFilename();
            Path tempDir = Files.createTempDirectory("audiveris_input");
            File inputFile = new File(tempDir.toFile(), filename);
            file.transferTo(inputFile);

            // Audiveris 실행
            String outputDir = tempDir.toFile().getAbsolutePath();
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-jar", AUDIVERIS_PATH + "/audiveris.jar", "-batch", "-export", inputFile.getAbsolutePath()
            );
            processBuilder.directory(new File(outputDir));
            Process process = processBuilder.start();
            process.waitFor();

            // 결과 파일 읽기
            File outputFile = new File(outputDir, filename + ".mxl");
            if (!outputFile.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing failed");
            }

            // 결과 반환
            return ResponseEntity.ok("File processed successfully: " + outputFile.getAbsolutePath());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }
}
