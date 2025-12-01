package com.example.share_terminal.controller.access;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.share_terminal.dto.UserResponse;
import com.example.share_terminal.service.UserAccessService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/access")
@AllArgsConstructor
public class AccessHandler {
  private UserAccessService userAccessService;

  @GetMapping("/connections")
  public Map<String, List<UserResponse>> getConnectedUsers(@AuthenticationPrincipal UserResponse userResponse) {
    return Map.of("connections", userAccessService.getConnectedUsers(userResponse.getId().toString()));
  }

  @GetMapping("/viewers")
  public Map<String, List<UserResponse>> getViewers(@AuthenticationPrincipal UserResponse userResponse) {
    return Map.of("viewers", userAccessService.getViewers(userResponse.getId().toString()));
  }

  @PostMapping("/request/{targetUserId}")
  public void requestAccess(@AuthenticationPrincipal UserResponse userResponse, @PathVariable String targetUserId) {
    userAccessService.requestAccess(userResponse.getId().toString(), targetUserId);
  }

  @GetMapping("/requests/incoming")
  public Map<String, List<UserResponse>> getIncomingRequests(@AuthenticationPrincipal UserResponse userResponse) {
    return Map.of("incomingRequests", userAccessService.getIncomingRequests(userResponse.getId().toString()));
  }

  @GetMapping("/requests/outgoing")
  public Map<String, List<UserResponse>> getOutgoingRequests(@AuthenticationPrincipal UserResponse userResponse) {
    return Map.of("outgoingRequests", userAccessService.getOutgoingRequests(userResponse.getId().toString()));
  }

  @PostMapping("/requests/{accessId}/accept")
  public void acceptRequest(@AuthenticationPrincipal UserResponse userResponse, @PathVariable UUID accessId) {
    userAccessService.acceptRequest(accessId, userResponse.getId().toString());
  }

  @PostMapping("/requests/{accessId}/reject")
  public void rejectRequest(@AuthenticationPrincipal UserResponse userResponse, @PathVariable UUID accessId) {
    userAccessService.rejectRequest(accessId, userResponse.getId().toString());
  }

}
