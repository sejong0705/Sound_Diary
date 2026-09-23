package com.sj.sound_diary.dto;

import java.time.LocalDateTime;

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
public class DiaryDto {

    private Long diaryId;
    private Long memberId;

    private String title;
    private String content;

    private Double latitude;
    private Double longitude;
    private String locationName;

    private String weatherIcon;
    private String weatherDesc;
    private Double weatherTemp;

    // 음원 관련은 지금은 null로 둠 (나중에 Spotify 연동 시 채움)
    private String trackName;
    private String artistName;
    private String albumImageUrl;
    private String spotifyTrackUri;

    private String isPublic;   // "Y" or "N"
    
    //목푝 화면 날짜 표시용
    private LocalDateTime createdAt;
    
    //공개 게시글 프로필 
    private String authorNickname;
    private String authorProfileImgUrl;
}