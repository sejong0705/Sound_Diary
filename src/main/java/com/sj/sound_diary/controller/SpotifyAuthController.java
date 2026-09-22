package com.sj.sound_diary.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.sj.sound_diary.service.MemberService;
import com.sj.sound_diary.service.SpotifyAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SpotifyAuthController {

    private final SpotifyAuthService spotifyAuthService;
    private final MemberService memberService;

    // 로그인 버튼이 이 URL로 이동하면, Spotify 인증 화면으로 리다이렉트
    @GetMapping("/api/auth/login")
    public RedirectView login() {
        return new RedirectView(spotifyAuthService.buildAuthorizeUrl());
    }
    
    @GetMapping("/api/auth/logout")
    public RedirectView logout(HttpSession session) {
        session.invalidate();  // 세션 완전히 무효화 (memberId, accessToken 등 다 날아감)
        return new RedirectView("/api/auth/login");
    }

    // Spotify가 인증 후 여기로 code를 담아 리다이렉트해줌
    @GetMapping("/api/auth/callback")
    public RedirectView callback(@RequestParam("code") String code, HttpSession session) {
        // 1. code -> token 교환
        Map<String, Object> tokenResponse = spotifyAuthService.exchangeCodeForToken(code);
        String accessToken = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");

        // 2. token으로 프로필 조회
        Map<String, Object> profile = spotifyAuthService.getSpotifyProfile(accessToken);
        String spotifyId = (String) profile.get("id");
        String nickname = (String) profile.get("display_name");
        String email = (String) profile.get("email");

        String profileImgUrl = null;
        var images = (List<Map<String, Object>>) profile.get("images");
        if (images != null && !images.isEmpty()) {
            profileImgUrl = (String) images.get(0).get("url");
        }

        // 3. MEMBER 테이블 저장/갱신
        Long memberId = memberService.loginOrRegister(spotifyId, nickname, email, profileImgUrl, refreshToken);

        // 4. 세션에 로그인 정보 저장 (이후 요청에서 로그인 여부 판단용)
        session.setAttribute("memberId", memberId);
        session.setAttribute("accessToken", accessToken);
        session.setAttribute("nickname", nickname);

        return new RedirectView("/app");
    }
}