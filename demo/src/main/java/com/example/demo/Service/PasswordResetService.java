package com.example.demo.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.PasswordReset;
import com.example.demo.Repository.PasswordResetRepository;

@Service
public class PasswordResetService {
    
    @Autowired
    private PasswordResetRepository passwordResetRepository;

    public boolean isTokenValid(String token) {
        Optional<PasswordReset> passwordResetOpt = passwordResetRepository.findByToken(token);
        if (passwordResetOpt.isPresent()) {
            PasswordReset passwordReset = passwordResetOpt.get();
            LocalDateTime currentTime = LocalDateTime.now();
            return currentTime.isBefore(passwordReset.getExpired());
        } else {
            return false; // Token not found
        }
    }
}
