package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.PushAccDTO;
import com.example.demo.Enum.UserRole;
import com.example.demo.Model.Users;
import com.example.demo.Repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users pushAcc(PushAccDTO pushAccDTO) {
        Users user = new Users();
        UserRole role = UserRole.valueOf(pushAccDTO.getRole());
        user.setEmail(pushAccDTO.getEmail());
        user.setFullname(pushAccDTO.getFullname());
        user.setPassword(passwordEncoder.encode(pushAccDTO.getPassword()));
        user.setRole(role);
        return userRepository.save(user);
    }

    public Users findByEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return userRepository.findByEmail(email).get();
        }
        return null;
    }
}
