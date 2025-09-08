package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.AuthRequest;
import com.example.demo.DTO.PushAccDTO;
import com.example.demo.Model.PasswordReset;
import com.example.demo.Model.Users;
import com.example.demo.Repository.PasswordResetRepository;
import com.example.demo.Service.PasswordResetService;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.JwtUtils;
import com.example.demo.Utils.SendResetEmail;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private SendResetEmail sendResetEmail;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private PasswordResetService passwordResetService;

    @Value("${jwt.expiration_time}")
    private Long EXPIRATION_TIME;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtUtils.generateToken(userDetails.getUsername(), roles);

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(false)
                    .path("/")
                    .maxAge(EXPIRATION_TIME / 1000)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
            // .body(Map.of("email", userDetails.getUsername(), "roles", roles));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy email"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Sai mật khẩu"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/push-acc")
    public ResponseEntity<?> pushAcc(@RequestBody PushAccDTO pushAccDTO) {
        Users users = userService.pushAcc(pushAccDTO);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        try {
            Users user = userService.findByEmail(email);
            if (user != null) {
                String token = UUID.randomUUID().toString();

                PasswordReset passwordReset = new PasswordReset();
                passwordReset.setEmail(email);
                passwordReset.setToken(token);
                passwordReset.setExpired(java.time.LocalDateTime.now().plusMinutes(10));
                passwordResetRepository.save(passwordReset);

                sendResetEmail.SendResetEmail(user.getEmail(), token);
            }
            return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/check-token/{token}")
    public ResponseEntity<?> checkTokenExpired(@PathVariable String token) {
        boolean isValid = passwordResetService.isTokenValid(token);
        if (isValid) {
            return ResponseEntity.ok(Map.of("valid", "Token is valid"));
        } else {
            return ResponseEntity.status(400).body(Map.of("error", "Token is invalid or expired"));
        }
    }
}
