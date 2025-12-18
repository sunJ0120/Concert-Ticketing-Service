package com.example.be.auth.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
    String provider = authToken.getAuthorizedClientRegistrationId();  // "kakao", "naver", "google"

    OAuth2User oAuth2User = authToken.getPrincipal();
    Map<String, Object> attributes = oAuth2User.getAttributes();

    String providerId;
    String email;
    String name;

    // provider별 분기
    if ("kakao".equals(provider)) {
      providerId = attributes.get("id")
          .toString();

      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      email = (String) kakaoAccount.get("email");

      Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
      name = (String) profile.get("nickname");

    } else if ("naver".equals(provider)) {
      Map<String, Object> response2 = (Map<String, Object>) attributes.get("response");
      providerId = (String) response2.get("id");
      email = (String) response2.get("email");
      name = (String) response2.get("name");

    } else if ("google".equals(provider)) {
      providerId = (String) attributes.get("sub");
      email = (String) attributes.get("email");
      name = (String) attributes.get("name");

    } else {
      throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
    }

    request.setAttribute("provider", provider);
    request.setAttribute("providerId", providerId);
    request.setAttribute("email", email);
    request.setAttribute("name", name);
    request.setAttribute("OAUTH2_AUTHENTICATED", true);

    request.getRequestDispatcher("/auth/login/social")
        .forward(request, response);
  }
}
