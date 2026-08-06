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
    public ResponseEntity<String> sslVerification(@PathVariable("filename") String filename) {
        
        // SSL 인증파일 내용 (2026-07 갱신분)
        String fileContent = "D31B4B6012FD52432EC9F85A20C7A983665C50080756B03874A5B552399E98F7\ncomodoca.com";

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