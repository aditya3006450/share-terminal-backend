package com.example.share_terminal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.share_terminal.security.JwtAuthFilter;

import java.util.List;

@Configuration
public class SecurityConfig {
  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 🔥 enable CORS here
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/public/**", "/ping").permitAll()
            .requestMatchers("/access/**", "/signal/**").authenticated()
            .anyRequest().authenticated())
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  // 🔥 THE IMPORTANT PART — CORS CONFIG
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(List.of("http://localhost:1420"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
