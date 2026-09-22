package com.sj.sound_diary.service.impl;

import com.sj.sound_diary.service.SpotifyAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyAuthServiceImpl implements SpotifyAuthService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    // Web Playback SDK 재생, 이메일/프로필 조회에 필요한 권한 범위
    private static final String SCOPES = "streaming user-read-email user-read-private";

    @Override
    public String buildAuthorizeUrl() {
        return UriComponentsBuilder.fromUriString("https://accounts.spotify.com/authorize")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", SCOPES)
                .queryParam("show_dialog", "true") //이거 테스트 용이라서 나중에 빼줘야함 (승인화면 띄워주는거임)
                .build()
                .toUriString();
    }

    @Override
    public Map<String, Object> exchangeCodeForToken(String code) {
        String url = "https://accounts.spotify.com/api/token";

        // client_id:client_secret을 Base64로 인코딩해 Basic 인증 헤더로 전달
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, request, Map.class);
    }

    @Override
    public Map<String, Object> getSpotifyProfile(String accessToken) {
        String url = "https://api.spotify.com/v1/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, request, Map.class).getBody();
    }
    
    @Override
    public Map<String, Object> refreshAccessToken(String refreshToken) {
        String url = "https://accounts.spotify.com/api/token";

        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, request, Map.class);
    }
}