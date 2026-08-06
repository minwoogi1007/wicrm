package com.wio.crm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
public class FileDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired
    private Environment env;

    @GetMapping("/download/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename) {
        try {
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                logger.warn("Path Traversal 시도 감지: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            String uploadDir = env.getProperty("file.upload-dir");
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(filename).normalize();

            if (!filePath.startsWith(basePath)) {
                logger.warn("허용 범위를 벗어난 파일 접근 시도: {}", filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("파일 다운로드 중 오류 발생: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("파일을 선택해주세요.", HttpStatus.BAD_REQUEST);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.contains("..") ||
                    originalFilename.contains("/") || originalFilename.contains("\\")) {
                return new ResponseEntity<>("잘못된 파일명입니다.", HttpStatus.BAD_REQUEST);
            }

            String uploadDir = env.getProperty("file.upload-dir");
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            String safeFilename = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
            Path filePath = basePath.resolve(safeFilename).normalize();

            if (!filePath.startsWith(basePath)) {
                return new ResponseEntity<>("잘못된 파일 경로입니다.", HttpStatus.BAD_REQUEST);
            }

            File saveFile = filePath.toFile();
            file.transferTo(saveFile);
            return new ResponseEntity<>("파일 업로드 성공: " + safeFilename, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("파일 업로드 중 오류 발생: {}", file.getOriginalFilename(), e);
            return new ResponseEntity<>("파일 업로드 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}