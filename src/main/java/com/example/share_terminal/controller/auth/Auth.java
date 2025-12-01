package com.example.share_terminal.controller.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.share_terminal.dto.UserResponse;
import com.example.share_terminal.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class Auth {

  private final UserService userService;

  @PostMapping("/login")
  @ResponseBody
  public HashMap<String, String> login(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String password = request.get("password");
    var token = userService.loginUser(email, password);
    HashMap<String, String> response = new HashMap<>();
    response.put("token", token);
    return response;
  }

  @PostMapping("/sign-up")
  @ResponseBody
  public ResponseEntity<UserResponse> signUp(@RequestBody Map<String, String> request) {
    UserResponse user = userService.registerUser(request.get("email"));
    return ResponseEntity.ok(user);
  }

  @GetMapping("/setup-password")
  public String setupPassword(@RequestParam("token") String token, @RequestParam("user_id") String user_id,
      Model model) {
    model.addAttribute("token", token);
    model.addAttribute("user_id", user_id);
    if (userService.verifyUserToken(user_id, token)) {
      return "setup-password";
    } else {
      model.addAttribute("error", "Invalid or expired token");
      return "error";
    }
  }

  @PostMapping("/setup-password")
  @ResponseBody
  public String processSetupPassword(
      @RequestParam("token") String token,
      @RequestParam("user_id") String user_id,
      @RequestParam("name") String name,
      @RequestParam("password") String password,
      Model model) {
    try {
      userService.setupPassword(token, user_id, name, password);
      return "password setup successful";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("token", token);
      return "password setup failed: " + e.getMessage();
    }
  }
}
