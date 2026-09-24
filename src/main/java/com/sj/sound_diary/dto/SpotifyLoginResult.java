package com.sj.sound_diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SpotifyLoginResult {
    private Long memberId;
    private String accessToken;
    private String nickname;
}
