package com.sj.sound_diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private Long memberId;
    private String spotifyId;
    private String nickname;
    private String email;
    private String profileImgUrl;
    private String refreshToken;
}