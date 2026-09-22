package com.sj.sound_diary.service;

import java.util.Map;

public interface SpotifyAuthService {
	// 로그인 버튼용: Spotify 인증 화면 URL 생성
    String buildAuthorizeUrl();

    // 콜백에서 받은 code를 access_token/refresh_token으로 교환
    Map<String, Object> exchangeCodeForToken(String code);

    // access_token으로 Spotify 사용자 프로필 조회 (spotify id, 닉네임, 이메일 등)
    Map<String, Object> getSpotifyProfile(String accessToken);
    
 // refresh_token으로 새 access_token 발급
    Map<String, Object> refreshAccessToken(String refreshToken);
}
