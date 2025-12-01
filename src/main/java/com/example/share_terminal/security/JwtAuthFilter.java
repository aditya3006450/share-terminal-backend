package com.example.share_terminal.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.share_terminal.dto.LoginTokenResponse;
import com.example.share_terminal.dto.UserResponse;
import com.example.share_terminal.service.JwtService;
import com.example.share_terminal.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserService userService;

  public JwtAuthFilter(JwtService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (path.startsWith("/auth") || path.equals("/ping")) {
      filterChain.doFilter(request, response);
      return;
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);
    String user_id = jwtService.extractUserId(token);
    UserResponse user = userService.getUserById(UUID.fromString(user_id));
    Set<String> userTokens = user.getTokens()
        .stream().map(LoginTokenResponse::getToken).collect(Collectors.toSet());

    if (!userTokens.contains(token) || !jwtService.validateToken(token, user_id)) {
      throw new RuntimeException("Token not found for this user");
    }
    List<GrantedAuthority> defaultAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        user, user_id, defaultAuthorities);
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
    filterChain.doFilter(request, response);
  }

}
