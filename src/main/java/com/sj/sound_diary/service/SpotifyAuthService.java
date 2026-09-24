package com.sj.sound_diary.service;

import com.sj.sound_diary.dto.SpotifyLoginResult;
import com.sj.sound_diary.dto.SpotifyProfileDto;
import com.sj.sound_diary.dto.SpotifyTokenResponseDto;

public interface SpotifyAuthService {
	// 로그인 버튼용: Spotify 인증 화면 URL 생성
    String buildAuthorizeUrl();

    // 콜백에서 받은 code를 access_token/refresh_token으로 교환
    SpotifyTokenResponseDto exchangeCodeForToken(String code);

    // access_token으로 Spotify 사용자 프로필 조회 (spotify id, 닉네임, 이메일 등)
    SpotifyProfileDto getSpotifyProfile(String accessToken);

 // refresh_token으로 새 access_token 발급
    SpotifyTokenResponseDto refreshAccessToken(String refreshToken);

    // OAuth 콜백 처리: 토큰 교환 -> 프로필 조회 -> 회원 upsert. 실패 시 null
    SpotifyLoginResult handleCallback(String code);
}
