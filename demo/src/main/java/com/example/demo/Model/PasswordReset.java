package com.example.demo.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {
    @Id
    @Column(name = "reset_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resetId;;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expired", nullable = false)
    private LocalDateTime expired;
}
