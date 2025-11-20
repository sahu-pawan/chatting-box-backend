// package com.chattingbox.controller;

// import com.chattingbox.config.JwtUtil;
// import com.chattingbox.model.User;
// import com.chattingbox.repository.UserRepository;
// import com.chattingbox.service.AuthService;

// import org.springframework.security.core.Authentication;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;

// import java.util.HashMap;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/auth")
// public class AuthController {

//     private final UserRepository userRepository;
//     private final JwtUtil jwtUtil;
//     private final PasswordEncoder passwordEncoder;

//     public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
//         this.userRepository = userRepository;
//         this.jwtUtil = jwtUtil;
//         this.passwordEncoder = passwordEncoder;
//     }

//     @PostMapping("/register")
//     public ResponseEntity<String> register(@RequestBody User user) {
//         if (userRepository.findByEmail(user.getEmail()) != null) {
//             return ResponseEntity.badRequest().body("⚠️ User already exists with email: " + user.getEmail());
//         }

//         user.setPassword(passwordEncoder.encode(user.getPassword())); // ✅ encode before saving
//         user.setProvider("local");
//         userRepository.save(user);

//         return ResponseEntity.ok("🎉 User registered successfully!");
//     }

//     @PostMapping("/login")
//     public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
//         String email = request.get("email");
//         String password = request.get("password");

//         User user = userRepository.findByEmail(email);
//         if (user == null) {
//             return ResponseEntity.status(401).body("Invalid credentials!");
//         }

//         if (!passwordEncoder.matches(password, user.getPassword())) {
//             return ResponseEntity.status(401).body("Invalid credentials!");
//         }

//         String token = jwtUtil.generateToken(email);

//         Map<String, String> response = new HashMap<>();
//         response.put("token", token);
//         response.put("username", user.getUsername());
//         response.put("email", user.getEmail());

//         return ResponseEntity.ok(response);
//     }

//     @GetMapping("/me")
//     public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
//         if (principal == null) {
//             return ResponseEntity.status(401).body("Not logged in");
//         }

//         if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
//             // Google login user
//             return ResponseEntity.ok(Map.of(
//                     "username", oAuth2User.getAttribute("name"),
//                     "email", oAuth2User.getAttribute("email")));
//         } else if (principal instanceof com.chattingbox.model.User user) {
//             // Manual JWT user
//             return ResponseEntity.ok(Map.of(
//                     "username", user.getUsername(),
//                     "email", user.getEmail()));
//         } else {
//             return ResponseEntity.status(401).body("Unknown user type");
//         }
//     }

// }
