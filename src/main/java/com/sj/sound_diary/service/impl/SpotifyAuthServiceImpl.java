package com.sj.sound_diary.service.impl;

import com.sj.sound_diary.dto.SpotifyLoginResult;
import com.sj.sound_diary.dto.SpotifyProfileDto;
import com.sj.sound_diary.dto.SpotifyTokenResponseDto;
import com.sj.sound_diary.service.MemberService;
import com.sj.sound_diary.service.SpotifyAuthService;
import com.sj.sound_diary.util.RestTemplateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
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
    private final MemberService memberService;

    // Web Playback SDK 재생, 이메일/프로필 조회에 필요한 권한 범위
    private static final String SCOPES = "streaming user-read-email user-read-private";


    //spotify 인증 화면 url 리다이렉트
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

    //토큰 발급 url 전송
    @Override
    public SpotifyTokenResponseDto exchangeCodeForToken(String code) {
        String url = "https://accounts.spotify.com/api/token";

        HttpHeaders headers = buildBasicAuthHeaders();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return RestTemplateUtils.post(restTemplate, url, request, SpotifyTokenResponseDto.class);
    }

    @Override
    public SpotifyProfileDto getSpotifyProfile(String accessToken) {
        String url = "https://api.spotify.com/v1/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        return RestTemplateUtils.get(restTemplate, url, request, SpotifyProfileDto.class);
    }

    @Override
    public SpotifyTokenResponseDto refreshAccessToken(String refreshToken) {
        String url = "https://accounts.spotify.com/api/token";

        HttpHeaders headers = buildBasicAuthHeaders();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return RestTemplateUtils.post(restTemplate, url, request, SpotifyTokenResponseDto.class);
    }

    @Override
    public SpotifyLoginResult handleCallback(String code) {
        try {
            // 1. code -> token 교환
            SpotifyTokenResponseDto tokenResponse = exchangeCodeForToken(code);
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                log.warn("Spotify 토큰 교환 응답이 비어있음");
                return null;
            }
            String accessToken = tokenResponse.getAccessToken(); //1시간
            String refreshToken = tokenResponse.getRefreshToken(); //토큰 재발급에 사용됨

            // 2. token으로 프로필 조회
            SpotifyProfileDto profile = getSpotifyProfile(accessToken);
            if (profile == null || profile.getId() == null) {
                log.warn("Spotify 프로필 응답이 비어있음");
                return null;
            }
            String spotifyId = profile.getId();
            String nickname = profile.getDisplayName();
            String email = profile.getEmail();

            String profileImgUrl = null;
            List<SpotifyProfileDto.Image> images = profile.getImages();
            if (images != null && !images.isEmpty()) {
                profileImgUrl = images.get(0).getUrl();
            }

            // 3. MEMBER 테이블 저장/갱신
            Long memberId = memberService.loginOrRegister(spotifyId, nickname, email, profileImgUrl, refreshToken);

            return SpotifyLoginResult.builder()
                    .memberId(memberId)
                    .accessToken(accessToken)
                    .nickname(nickname)
                    .build();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // code가 만료/재사용되었거나 Spotify 쪽 오류로 토큰 교환·프로필 조회가 실패한 경우
            log.warn("Spotify 로그인 콜백 처리 실패: {}", e.getMessage());
            return null;
        }
    }

    // client_id:client_secret을 Base64로 인코딩한 Basic 인증 헤더 (토큰 교환/갱신 공통)
    private HttpHeaders buildBasicAuthHeaders() {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }
}