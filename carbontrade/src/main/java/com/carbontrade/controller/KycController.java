package com.carbontrade.controller;

import com.carbontrade.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/kyc")
@CrossOrigin(origins = "*")
public class KycController {

    @Autowired
    private KycService kycService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadKyc(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(kycService.uploadKycDocument(username, file));
    }

    @GetMapping("/status")
    public ResponseEntity<String> getKycStatus(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(kycService.getKycStatus(username));
    }
}
