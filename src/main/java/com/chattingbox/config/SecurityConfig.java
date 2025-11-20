package com.chattingbox.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.chattingbox.service.CustomUserDetailsService;

// @Configuration
// public class SecurityConfig {

//         @Autowired
//         private CustomUserDetailsService customUserDetailsService;

//         @Bean
//         public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//                 http.csrf(csrf -> csrf.disable())
//                                 .authorizeHttpRequests(auth -> auth
//                                                 .requestMatchers("/", "/bgpic/**", "/login.html", "/api/auth/**",
//                                                                 "/css/**",
//                                                                 "/js/**", "/static/**", "/ws/**")
//                                                 .permitAll()
//                                                 .anyRequest().authenticated())
//                                 .oauth2Login(oauth2 -> oauth2
//                                                 // After successful OAuth2 login, forward to our controller which
//                                                 // will create the user if necessary and then redirect to chat page
//                                                 .defaultSuccessUrl("/api/auth/google/success", true))
//                                 .formLogin(form -> form
//                                                 .loginPage("/login.html")
//                                                 .defaultSuccessUrl("/api/auth/google/success", true));

//                 return http.build();
//         }



        @Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
                private CustomUserDetailsService customUserDetailsService;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                                .userDetailsService(customUserDetailsService)
                                .passwordEncoder(passwordEncoder)
                                .and()
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}