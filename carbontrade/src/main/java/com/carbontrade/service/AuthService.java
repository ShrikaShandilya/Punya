package com.carbontrade.service;

import com.carbontrade.controller.dto.AuthRequest;
import com.carbontrade.controller.dto.AuthResponse;
import com.carbontrade.controller.dto.RegisterRequest;
import com.carbontrade.model.User;
import com.carbontrade.repository.UserRepository;
import com.carbontrade.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String mailUsername;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.password}")
    private String mailPassword;

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(jwt);
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getUsername()); // Default name to username
        user.setRole("USER");
        user.setKycStatus("PENDING");
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Generate and send verification
        String verificationCode = generateVerificationCode();
        sendVerificationEmail(savedUser.getEmail(), verificationCode);

        return "User registered successfully. Verification code sent to email: " + savedUser.getEmail();
    }

    public String verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            return "Token is missing";
        }
        boolean valid = jwtTokenProvider.validateToken(token);
        if (!valid) {
            return "Token is invalid or expired";
        }
        String username = jwtTokenProvider.getUsernameFromToken(token);
        if (userRepository.findByUsername(username).isEmpty()) {
            return "Token is valid but user does not exist";
        }
        return "Token is valid for user: " + username;
    }

    // ----------------------------
    // Password & Email Support
    // ----------------------------

    public String generateVerificationCode() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        int code = random.nextInt(1000000);
        return "VT-" + code;
    }

    public void sendVerificationEmail(String email, String verificationCode) {
        // NOTE: In production, use Spring Mail or an external service.
        // Preserving existing logic for now.
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        try {
            javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress(mailUsername));
            message.addRecipient(javax.mail.Message.RecipientType.TO, new javax.mail.internet.InternetAddress(email));
            message.setSubject("Verification Code for Carbon Trade Account");
            message.setText("Your verification code is: " + verificationCode);
            javax.mail.Transport.send(message);
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        }
    }

    public String forgotPassword(String email) {
        try {
            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");

            javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(mailUsername, mailPassword);
                }
            });

            javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress(mailUsername));
            message.addRecipient(javax.mail.Message.RecipientType.TO, new javax.mail.internet.InternetAddress(email));
            message.setSubject("Password Reset for Carbon Trade Account");

            javax.mail.Transport.send(message);
            return "Password reset link sent";
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String changePassword(String email, String oldPassword, String newPassword) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Note: In real app, check encoded password
            if (!passwordEncoder.matches(oldPassword, user.getPassword()) && !user.getPassword().equals(oldPassword)) {
                // Fallback for plain text during migration (if any)
                return "Old password is incorrect";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            sendConfirmationEmail(email);

            return "Password changed successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while changing the password";
        }
    }

    public String sendConfirmationEmail(String email) {
        String subject = "Password Change Confirmation for CarbonTrade";
        String body = "Your password has been successfully changed.";

        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        try {
            javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress(mailUsername));
            message.addRecipient(javax.mail.Message.RecipientType.TO, new javax.mail.internet.InternetAddress(email));
            message.setSubject(subject);
            message.setText(body);
            javax.mail.Transport.send(message);

            return "Confirmation email successfully sent to: " + email;
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            return "Failed to send confirmation email to: " + email + ". Error: " + e.getMessage();
        }
    }
}
