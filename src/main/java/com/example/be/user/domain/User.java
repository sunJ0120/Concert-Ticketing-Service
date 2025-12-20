package com.example.be.user.domain;

import com.example.be.common.BaseEntity;
import com.example.be.user.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)    // 생성/수정 시간 자동 관리를 위해 추가
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "이메일은 필수입니다")
  @Column(unique = true, nullable = false, length = 255)
  private String email;

  @Column(length = 255)
  private String password;

  @NotBlank(message = "이름은 필수입니다")
  @Size(min = 2, max = 100, message = "이름은 2~100자 사이여야 합니다")
  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role = Role.USER;

  @Builder
  public User(String email, String password, String name, Role role) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.role = role != null ? role : Role.USER;
  }

  public static User createUser(String email, String password, String name) {
    return User.builder()
        .email(email)
        .password(password)
        .name(name)
        .role(Role.USER)
        .build();
  }

  public void grantAdminRole() {
    this.role = Role.ADMIN;
  }
}
