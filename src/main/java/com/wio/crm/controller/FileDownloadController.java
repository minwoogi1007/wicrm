package com.wio.crm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
@RestController
public class FileDownloadController {

    @GetMapping("/download/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());
            Path fileStorageLocation = Paths.get("/down").toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve(decodedFilename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                String encodedFilename = UriUtils.encodePathSegment(resource.getFilename(), StandardCharsets.UTF_8.name());
                String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename;
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                        .body(resource);
            } else {
                System.out.println("File not found: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}