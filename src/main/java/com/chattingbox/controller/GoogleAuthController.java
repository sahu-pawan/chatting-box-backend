// package com.chattingbox.controller;

// import com.chattingbox.model.User;
// import com.chattingbox.repository.UserRepository;
// import jakarta.servlet.http.HttpServletResponse;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;

// @RestController
// @RequestMapping("/api/auth")
// public class GoogleAuthController {

//     private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

//     @GetMapping("/google/success")
//     public void googleSuccess(@AuthenticationPrincipal OAuth2User principal,
//             HttpServletResponse response) {
//         try {
//             if (principal == null) {
//                 logger.warn("googleSuccess called with null principal");
//                 response.sendRedirect("/login.html?error=auth");
//                 return;
//             }

//             String email = principal.getAttribute("email");
//             String name = principal.getAttribute("name");

//             if (email == null) {
//                 logger.warn("OAuth2 principal missing email attribute: {}", principal.getAttributes());
//                 response.sendRedirect("/login.html?error=missing_email");
//                 return;
//             }

//             User existingUser = userRepository.findByEmail(email);

//             String msg;
//             if (existingUser == null) {
//                 // create new user for Google login
//                 try {
//                     User newUser = new User();
//                     newUser.setEmail(email);
//                     newUser.setUsername(name != null ? name : email);
//                     newUser.setProvider("google");
//                     // generate a random password and store the bcrypt hash, so user record has a
//                     // password if they later want to login with username/password or reset it
//                     String randomPass = java.util.UUID.randomUUID().toString();
//                     newUser.setPassword(passwordEncoder.encode(randomPass));
//                     userRepository.save(newUser);
//                     msg = "new";
//                     logger.info("Created new OAuth user: {}", email);
//                 } catch (Exception ex) {
//                     logger.error("Failed to save new OAuth user {}", email, ex);
//                     response.sendRedirect("/login.html?error=save_failed");
//                     return;
//                 }
//             } else {
//                 msg = "welcome";
//             }

//             // redirect to chat page with query param (client will pick name)
//             response.sendRedirect("/chat_call.html?google=" + msg + "&name="
//                     + java.net.URLEncoder.encode(name != null ? name : email, java.nio.charset.StandardCharsets.UTF_8));
//         } catch (Exception e) {
//             logger.error("Unexpected error in googleSuccess", e);
//             try {
//                 response.sendRedirect("/login.html?error=server");
//             } catch (Exception ex) {
//                 logger.error("Redirect failed", ex);
//             }
//         }
//     }

// }
