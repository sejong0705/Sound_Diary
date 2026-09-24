package com.sj.sound_diary.controller;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.sj.sound_diary.dto.SpotifyLoginResult;
import com.sj.sound_diary.service.SpotifyAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SpotifyAuthController {

    private final SpotifyAuthService spotifyAuthService;

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
        SpotifyLoginResult result = spotifyAuthService.handleCallback(code);
        if (result == null) {
            return new RedirectView("/api/auth/login");
        }

        // 세션에 로그인 정보 저장 (이후 요청에서 로그인 여부 판단용)
        session.setAttribute("memberId", result.getMemberId());
        session.setAttribute("accessToken", result.getAccessToken());
        session.setAttribute("nickname", result.getNickname());

        return new RedirectView("/app");
    }
}
