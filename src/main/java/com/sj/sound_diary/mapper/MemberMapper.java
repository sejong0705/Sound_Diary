package com.sj.sound_diary.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.sj.sound_diary.dto.MemberDto;

@Mapper
public interface MemberMapper {
	MemberDto selectBySpotifyId(String spotify);
	
	void insertMember(MemberDto member);
	
	void updateLoginInfo(MemberDto member);
	
	//refresh Token 관리
	String selectRefreshTokenByMemberId(Long memberId);
}
