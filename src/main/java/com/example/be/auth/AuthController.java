package com.example.be.auth;

import com.example.be.auth.dto.LoginRequest;
import com.example.be.auth.dto.LoginResponse;
import com.example.be.auth.dto.SignupRequest;
import com.example.be.auth.service.AuthService;
import com.example.be.auth.util.AuthHttpHelper;
import com.example.be.auth.validator.AuthValidator;
import com.example.be.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final AuthValidator authValidator;
  private final AuthHttpHelper authHttpHelper;
  private final JwtUtils jwtUtils;

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일")})
  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
    authService.signup(request);
    return ResponseEntity.ok("회원가입 성공");
  }

  @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인합니다.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 이메일 또는 비밀번호")})
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);

    // refreshToken 분리해서 따로 HttpOnly에 저장
    String refreshToken = response.refreshToken();
    ResponseCookie responseCookie = authHttpHelper.getResponseCookie(refreshToken);

    response = new LoginResponse(response.accessToken(), null);

    // accesss token만 전달
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(response);
  }

  @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하여 로그아웃 처리합니다.", security = {
      @SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패")})
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String token = jwtUtils.resolveToken(request);

    try {
      token = authValidator.validateAndGetToken(token);
    } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .build();
    }

    authService.logout(token);
    ResponseCookie cookie = authHttpHelper.createLogoutCookie();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
    String refreshToken = authHttpHelper.extractRefreshToken(request);

    try {
      refreshToken = authValidator.validateAndGetToken(refreshToken);
    } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .build();
    }

    LoginResponse response;

    try {
      response = authService.refresh(refreshToken);
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .build();
    }

    ResponseCookie responseCookie = authHttpHelper.getResponseCookie(refreshToken);
    response = new LoginResponse(response.accessToken(), null);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(response);
  }
}
