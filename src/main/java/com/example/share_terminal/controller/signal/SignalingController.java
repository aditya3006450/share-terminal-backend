package com.example.share_terminal.controller.signal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.share_terminal.dto.UserResponse;
import com.example.share_terminal.service.SignalingService;
import com.example.share_terminal.service.SignalingService.SignalMessage;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/signal")
@AllArgsConstructor
public class SignalingController {

  private final SignalingService signalingService;

  @PostMapping("/send")
  public void sendSignal(@AuthenticationPrincipal UserResponse user,
      @RequestBody Map<String, Object> body) {
    String toUserIdStr = (String) body.get("toUserId");
    String type = (String) body.get("type");
    @SuppressWarnings("unchecked")
    Map<String, Object> payload = (Map<String, Object>) body.getOrDefault("payload", new HashMap<>());
    if (toUserIdStr == null || type == null) {
      throw new RuntimeException("Missing toUserId or type");
    }
    UUID fromUserId = user.getId();
    UUID toUserId = UUID.fromString(toUserIdStr);
    signalingService.sendSignal(fromUserId, toUserId, type, payload);
  }

  @GetMapping("/inbox")
  public Map<String, List<SignalMessage>> getInbox(@AuthenticationPrincipal UserResponse user) {
    List<SignalMessage> messages = signalingService.fetchAndClearInbox(user.getId());
    Map<String, List<SignalMessage>> response = new HashMap<>();
    response.put("messages", messages);
    return response;
  }
}
