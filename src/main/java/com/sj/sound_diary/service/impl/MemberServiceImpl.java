package com.sj.sound_diary.service.impl;

import com.sj.sound_diary.dto.MemberDto;
import com.sj.sound_diary.mapper.MemberMapper;
import com.sj.sound_diary.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;

    @Override
    public Long loginOrRegister(String spotifyId, String nickname, String email,
                                  String profileImgUrl, String refreshToken) {

        MemberDto existing = memberMapper.selectBySpotifyId(spotifyId);

        MemberDto member = MemberDto.builder()
                .spotifyId(spotifyId)
                .nickname(nickname)
                .email(email)
                .profileImgUrl(profileImgUrl)
                .refreshToken(refreshToken)
                .build();

        if (existing == null) {
            memberMapper.insertMember(member);
            // 방금 INSERT한 회원의 ID를 다시 조회 (시퀀스+트리거 구조라 별도 selectKey 없이 이렇게 처리)
            return memberMapper.selectBySpotifyId(spotifyId).getMemberId();
        } else {
            memberMapper.updateLoginInfo(member);
            return existing.getMemberId();
        }
    }
    
    @Override
    public String getRefreshToken(Long memberId) {
        return memberMapper.selectRefreshTokenByMemberId(memberId);
    }
}