package com.sj.sound_diary.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.sj.sound_diary.dto.TrackDto;
import com.sj.sound_diary.service.MemberService;
import com.sj.sound_diary.service.SpotifyAuthService;
import com.sj.sound_diary.service.SpotifyService;
import com.sj.sound_diary.util.SessionUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final SpotifyAuthService spotifyAuthService;
    private final MemberService memberService;

    // 좌측 검색 패널에서 사용
    @GetMapping("/search")
    public List<TrackDto> search(@RequestParam String q, HttpSession session) {
        String accessToken = SessionUtils.attr(session, "accessToken");

        try {
            return spotifyService.searchTracks(q, accessToken);
        } catch (HttpClientErrorException.Unauthorized e) {
            String newAccessToken = refreshAndUpdateSession(session);
            return spotifyService.searchTracks(q, newAccessToken);
        }
    }

    // getToken(): 세션에 있는 토큰을 그냥 그대로 반환 (검증 없음)
    @GetMapping("/token")
    public Map<String, String> getToken(HttpSession session) {
        String accessToken = SessionUtils.attr(session, "accessToken");

        try {
            spotifyAuthService.getSpotifyProfile(accessToken);
        } catch (HttpClientErrorException.Unauthorized e) {
            accessToken = refreshAndUpdateSession(session);
        }

        return Map.of("accessToken", accessToken);
    }

    // refreshAndUpdateSession(): refresh_token으로 새 토큰 발급받아 세션 갱신
    private String refreshAndUpdateSession(HttpSession session) {
        Long memberId = SessionUtils.attr(session, "memberId");
        String refreshToken = memberService.getRefreshToken(memberId);
        Map<String, Object> result = spotifyAuthService.refreshAccessToken(refreshToken);
        String newAccessToken = (String) result.get("access_token");
        session.setAttribute("accessToken", newAccessToken);
        return newAccessToken;
    }
    
    @GetMapping("/recommendation")
    public TrackDto getRecommendation(
            @RequestParam String artistId,
            @RequestParam String excludeUri,
            HttpSession session) {
        String accessToken = SessionUtils.attr(session, "accessToken");

        try {
            return spotifyService.getRecommendation(artistId, excludeUri, accessToken);
        } catch (HttpClientErrorException.Unauthorized e) {
            String newAccessToken = refreshAndUpdateSession(session);
            return spotifyService.getRecommendation(artistId, excludeUri, newAccessToken);
        }
    }
}