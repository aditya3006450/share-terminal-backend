package com.example.share_terminal.service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;

  public String createPasswordSetToken(UUID user_id) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(user_id.toString())
        .setIssuedAt(now)
        .signWith(getSigningKey())
        .compact();
  }

  public String extractUserId(String token) {
    return extractAllClaims(token).getSubject();
  }

  public boolean validateToken(String token, String user_id) {
    return user_id.equals(extractUserId(token));
  }

  public boolean isExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }

  private Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
