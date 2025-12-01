package com.example.share_terminal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.share_terminal.entity.LoginToken;

@Repository
public interface LoginTokenRepository extends JpaRepository<LoginToken, UUID> {
}
