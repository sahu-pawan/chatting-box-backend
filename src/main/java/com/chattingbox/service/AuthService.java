package com.chattingbox.service;

import com.chattingbox.model.User;
import com.chattingbox.repository.UserRepository;
import com.chattingbox.config.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // Register user
    public String register(String username, String email, String password) {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("User already exists!");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider("manual");
        userRepository.save(user);

        return jwtUtil.generateToken(email); // return JWT after register
    }

    // Login user
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials!");
        }
        return jwtUtil.generateToken(email); // return JWT after login
    }

    // Google login
    public String googleLogin(String email, String username) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setProvider("google");
            userRepository.save(user);
        }
        return jwtUtil.generateToken(email);
    }
}
