package com.wio.crm.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SslController {

    /**
     * 카페24 SSL 인증을 위한 컨트롤러
     */
    @GetMapping("/.well-known/pki-validation/{filename}")
    public ResponseEntity<String> sslVerification(@PathVariable String filename) {
        
        // SSL 인증파일 내용
        String fileContent = "537A01E0CB1E3AE1866CC215230FEB7BF61E01779C0FE17CC5989A5BE24BF946\ncomodoca.com";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileContent);
    }
    
    // 특정 파일명으로도 접근 가능하도록
    @GetMapping("/.well-known/pki-validation/EDBA8B6CCECA0D2D17908BEF168F0C23.txt")
    public ResponseEntity<String> sslVerificationSpecific() {
        
        String fileContent = "537A01E0CB1E3AE1866CC215230FEB7BF61E01779C0FE17CC5989A5BE24BF946\ncomodoca.com";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileContent);
    }
    
    // 디버깅용
    @GetMapping("/ssl-debug")
    public String debug() {
        return "SSL Controller is working on port 80!";
    }
} 