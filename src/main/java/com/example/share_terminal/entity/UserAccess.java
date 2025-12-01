package com.example.share_terminal.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccess {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "is_connection_accepted")
  private Boolean isConnectionAccepted = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "connected_from_user_id")
  private User connectedFrom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "connected_to_user_id")
  private User connectedTo;

}
