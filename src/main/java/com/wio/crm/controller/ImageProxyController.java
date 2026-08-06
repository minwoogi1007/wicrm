package com.wio.crm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Controller
@RequestMapping("/api")
public class ImageProxyController {
    
    private final Logger logger = LoggerFactory.getLogger(ImageProxyController.class);

    @Value("${app.file-server.url}${app.file-server.upload-path}")
    private String externalServer;

    /**
     * 외부 서버에서 이미지를 직접 가져와서 반환하는 단순 프록시
     */
    private static final java.util.regex.Pattern ALLOWED_PATH_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9가-힣/_.-]+$");

    private boolean isValidFilePath(String path) {
        if (path == null || path.isEmpty()) return false;
        if (path.contains("..") || path.contains("://") || path.contains("\\")) return false;
        return ALLOWED_PATH_PATTERN.matcher(path).matches();
    }

    @GetMapping("/image-proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("path") String filePath) {
        logger.info("이미지 프록시 요청: {}", filePath);

        if (!isValidFilePath(filePath)) {
            logger.warn("잘못된 이미지 경로 요청: {}", filePath);
            return ResponseEntity.badRequest().build();
        }

        try {
            URL url = new URL(externalServer + filePath);
            logger.info("요청 URL: {}", url);
            
            // 연결 설정
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            logger.info("응답 코드: {}", responseCode);
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("서버 응답 오류: {}", responseCode);
                return ResponseEntity.status(responseCode).build();
            }
            
            // 콘텐츠 타입 확인
            String contentType = connection.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg"; // 기본값
            }
            logger.info("콘텐츠 타입: {}", contentType);
            
            // 데이터 읽기
            InputStream inputStream = connection.getInputStream();
            byte[] imageData = inputStream.readAllBytes();
            inputStream.close();
            logger.info("이미지 크기: {} 바이트", imageData.length);
            
            // 응답 반환
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);
            
        } catch (Exception e) {
            logger.error("이미지 프록시 오류:", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 파일 다운로드용 단순 프록시
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("path") String filePath) {
        logger.info("파일 다운로드 요청: {}", filePath);

        if (!isValidFilePath(filePath)) {
            logger.warn("잘못된 다운로드 경로 요청: {}", filePath);
            return ResponseEntity.badRequest().build();
        }

        try {
            URL url = new URL(externalServer + filePath);
            
            // 연결 설정
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("서버 응답 오류: {}", responseCode);
                return ResponseEntity.status(responseCode).build();
            }
            
            // 콘텐츠 타입 확인
            String contentType = connection.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 데이터 읽기
            InputStream inputStream = connection.getInputStream();
            byte[] fileData = inputStream.readAllBytes();
            inputStream.close();
            logger.info("파일 크기: {} 바이트", fileData.length);
            
            // 파일명 추출
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            
            // 응답 반환
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(fileData);
            
        } catch (Exception e) {
            logger.error("파일 다운로드 오류:", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}