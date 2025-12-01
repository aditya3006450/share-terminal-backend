package com.example.share_terminal.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.example.share_terminal.repository.UserAccessRepository;
import com.example.share_terminal.repository.UserRepository;
import com.example.share_terminal.dto.UserResponse;
import com.example.share_terminal.entity.User;
import com.example.share_terminal.entity.UserAccess;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccessService {
  private final UserAccessRepository userAccessRepository;
  private final UserRepository userRepository;

  public List<UserResponse> getConnectedUsers(String userId) {
    var usersAccess = userAccessRepository.findByConnectedFromIdAndIsConnectionAcceptedTrue(UUID.fromString(userId));
    return usersAccess.stream()
        .map(access -> toResponse(access.getConnectedTo()))
        .collect(Collectors.toList());
  }

  public List<UserResponse> getViewers(String userId) {
    var usersAccess = userAccessRepository.findByConnectedToIdAndIsConnectionAcceptedTrue(UUID.fromString(userId));
    return usersAccess.stream()
        .map(access -> toResponse(access.getConnectedFrom()))
        .collect(Collectors.toList());
  }

  public void requestAccess(String fromUserId, String toUserId) {
    User fromUser = userRepository.getReferenceById(UUID.fromString(fromUserId));
    User toUser = userRepository.getReferenceById(UUID.fromString(toUserId));

    UserAccess access = new UserAccess();
    access.setConnectedFrom(fromUser);
    access.setConnectedTo(toUser);
    access.setIsConnectionAccepted(false);
    userAccessRepository.save(access);
  }

  public List<UserResponse> getIncomingRequests(String userId) {
    var accessList = userAccessRepository.findByConnectedToIdAndIsConnectionAcceptedFalse(UUID.fromString(userId));
    return accessList.stream()
        .map(access -> toResponse(access.getConnectedFrom()))
        .collect(Collectors.toList());
  }

  public List<UserResponse> getOutgoingRequests(String userId) {
    var accessList = userAccessRepository.findByConnectedFromIdAndIsConnectionAcceptedFalse(UUID.fromString(userId));
    return accessList.stream()
        .map(access -> toResponse(access.getConnectedTo()))
        .collect(Collectors.toList());
  }

  public void acceptRequest(UUID accessId, String userId) {
    UserAccess access = userAccessRepository.findById(accessId).orElseThrow();
    if (!access.getConnectedTo().getId().equals(UUID.fromString(userId))) {
      throw new RuntimeException("Not allowed to accept this request");
    }
    access.setIsConnectionAccepted(true);
    userAccessRepository.save(access);
  }

  public void rejectRequest(UUID accessId, String userId) {
    UserAccess access = userAccessRepository.findById(accessId).orElseThrow();
    if (!access.getConnectedTo().getId().equals(UUID.fromString(userId))) {
      throw new RuntimeException("Not allowed to reject this request");
    }
    userAccessRepository.delete(access);
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
