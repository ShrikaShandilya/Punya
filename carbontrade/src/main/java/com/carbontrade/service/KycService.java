package com.carbontrade.service;

import com.carbontrade.model.User;
import com.carbontrade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class KycService {

    private final Path rootLocation = Paths.get("kyc-uploads");

    @Autowired
    private UserRepository userRepository;

    public KycService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String storeFile(MultipartFile file, String username) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            String filename = username + "_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
            return filename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    public String uploadKycDocument(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String filename = storeFile(file, username);

        user.setKycStatus("SUBMITTED");
        // In a real app, we would store the filename/path in a separate KYC table or
        // field
        // For now, assuming just status update is enough for prototype

        userRepository.save(user);

        return "KYC document uploaded successfully. Status: SUBMITTED. File: " + filename;
    }

    public String getKycStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getKycStatus();
    }
}
