package com.sj.sound_diary.service;

public interface MemberService {
	// Spotify 프로필로 회원 조회/생성/갱신 후 memberId 반환
    Long loginOrRegister(String spotifyId, String nickname, String email,
                          String profileImgUrl, String refreshToken);
}
