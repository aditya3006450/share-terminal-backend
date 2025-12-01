package com.example.share_terminal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.share_terminal.entity.UserAccess;

@Repository
public interface UserAccessRepository extends JpaRepository<UserAccess, UUID> {
  List<UserAccess> findByConnectedFromIdAndIsConnectionAcceptedTrue(UUID userId);

  List<UserAccess> findByConnectedToIdAndIsConnectionAcceptedTrue(UUID userId);

  List<UserAccess> findByConnectedToIdAndIsConnectionAcceptedFalse(UUID userId);

  List<UserAccess> findByConnectedFromIdAndIsConnectionAcceptedFalse(UUID userId);
}
