package com.example.share_terminal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.share_terminal.entity.UserAccess;
import com.example.share_terminal.repository.UserAccessRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import tools.jackson.databind.ObjectMapper;

@Service
public class SignalingService {

  private final UserAccessRepository userAccessRepository;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public SignalingService(UserAccessRepository userAccessRepository, StringRedisTemplate redisTemplate) {
    this.userAccessRepository = userAccessRepository;
    this.redisTemplate = redisTemplate;
  }

  public void sendSignal(UUID fromUserId, UUID toUserId, String type, Map<String, Object> payload) {
    if (!hasAccessBetween(fromUserId, toUserId)) {
      throw new RuntimeException("No access between users");
    }
    SignalMessage msg = new SignalMessage(fromUserId, toUserId, type, payload);
    String key = inboxKey(toUserId);
    try {
      String json = objectMapper.writeValueAsString(msg);
      redisTemplate.opsForList().rightPush(key, json);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize signal message", e);
    }
  }

  public List<SignalMessage> fetchAndClearInbox(UUID userId) {
    String key = inboxKey(userId);
    List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
    redisTemplate.delete(key);
    List<SignalMessage> result = new ArrayList<>();
    if (raw == null) {
      return result;
    }
    for (String item : raw) {
      try {
        result.add(objectMapper.readValue(item, SignalMessage.class));
      } catch (Exception e) {
        // skip invalid entries
      }
    }
    return result;
  }

  private boolean hasAccessBetween(UUID a, UUID b) {
    List<UserAccess> fromA = userAccessRepository.findByConnectedFromIdAndIsConnectionAcceptedTrue(a);
    boolean aToB = fromA.stream().anyMatch(ua -> ua.getConnectedTo() != null && b.equals(ua.getConnectedTo().getId()));
    if (aToB) {
      return true;
    }
    List<UserAccess> fromB = userAccessRepository.findByConnectedFromIdAndIsConnectionAcceptedTrue(b);
    boolean bToA = fromB.stream().anyMatch(ua -> ua.getConnectedTo() != null && a.equals(ua.getConnectedTo().getId()));
    return bToA;
  }

  private String inboxKey(UUID userId) {
    return "signal:inbox:" + userId.toString();
  }

  @Data
  @AllArgsConstructor
  public static class SignalMessage {
    private UUID fromUserId;
    private UUID toUserId;
    private String type;
    private Map<String, Object> payload;
  }
}
