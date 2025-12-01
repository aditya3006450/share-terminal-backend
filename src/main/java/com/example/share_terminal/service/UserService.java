package com.example.share_terminal.service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.share_terminal.dto.LoginTokenResponse;
import com.example.share_terminal.dto.UserResponse;
import com.example.share_terminal.entity.LoginToken;
import com.example.share_terminal.entity.User;
import com.example.share_terminal.repository.LoginTokenRepository;
import com.example.share_terminal.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final LoginTokenRepository loginTokenRepository;
  private final JwtService jwtService;
  private final MailService mailService;

  @Transactional
  public UserResponse getUserById(UUID id) {
    User user = userRepository.getReferenceById(id);
    Set<LoginToken> tokens = user.getTokens();
    Set<LoginTokenResponse> tokenResponses = tokens.stream()
        .map(LoginTokenResponse::fromLoginToken)
        .collect(Collectors.toSet());
    return UserResponse.builder().id(user.getId()).email(user.getEmail()).tokens(tokenResponses).build();
  }

  public String loginUser(String email, String password) {
    User user = userRepository.findByEmail(email).orElseThrow();
    boolean ok = new BCryptPasswordEncoder().matches(password, user.getPassword());
    if (!ok) {
      throw new RuntimeException("Invalid credentials");
    }
    String token = jwtService.createPasswordSetToken(user.getId());
    LoginToken loginToken = new LoginToken();
    loginToken.setToken(token);
    loginToken.setUser(user);
    loginTokenRepository.save(loginToken);
    user.getTokens().add(loginToken);
    userRepository.save(user);
    return token;
  }

  public UserResponse registerUser(String email) {
    userRepository.findByEmail(email).ifPresent(u -> {
      throw new RuntimeException("User with this email already exists");
    });
    User user = new User();
    LoginToken loginToken = new LoginToken();

    user.setEmail(email);
    loginToken.setUser(user);
    User savedUser = userRepository.save(user);

    loginToken.setToken(jwtService.createPasswordSetToken(savedUser.getId()));
    savedUser.getTokens().add(loginToken);

    loginTokenRepository.save(loginToken);
    userRepository.save(savedUser);

    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    String link = baseUrl + "/auth/setup-password?token=" + loginToken.getToken() + "&user_id=" + savedUser.getId();
    mailService.sendPasswordSetupMail(email, link);

    return toResponse(user);
  }

  public void setupPassword(String token, String user_id, String name, String password) {
    User user = userRepository.getReferenceById(UUID.fromString(user_id));
    if (user == null) {
      throw new RuntimeException("User not found");
    }
    verifyUserToken(user_id, token);
    user.setName(name);
    user.setPassword(new BCryptPasswordEncoder().encode(password));
    user.setIs_verified("true");
    userRepository.save(user);
  }

  public Boolean verifyUserToken(String user_id, String token) {
    User user = userRepository.getReferenceById(UUID.fromString(user_id));
    if (user == null) {
      return false;
    }
    return user.getTokens()
        .stream().map(LoginToken::getToken)
        .collect(Collectors.toSet()).contains(token);
  }

  public UserResponse toResponse(User user) {
    return UserResponse
        .builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .password(user.getPassword())
        .build();
  }

}
