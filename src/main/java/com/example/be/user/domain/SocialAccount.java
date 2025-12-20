package com.example.be.user.domain;

import com.example.be.common.BaseTimeEntity;
import com.example.be.user.enums.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "social_accounts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_provider_account",
            columnNames = {"provider", "provider_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_social_account_user"))
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Provider provider;

  @Column(name = "provider_id", nullable = false, length = 255)
  private String providerId;

  @Builder
  public SocialAccount(User user, Provider provider, String providerId) {
    this.user = user;
    this.provider = provider;
    this.providerId = providerId;
  }
}
