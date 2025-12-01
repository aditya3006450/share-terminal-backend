package com.example.share_terminal.dto;

import java.util.UUID;

import com.example.share_terminal.entity.LoginToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginTokenResponse {
  private UUID id;
  private String token;

  public static LoginTokenResponse fromLoginToken(LoginToken loginToken) {
    return LoginTokenResponse.builder()
        .id(loginToken.getId())
        .token(loginToken.getToken())
        .build();
  }
}
