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
public class TrackDto {
    private String name;
    private String artistName;
    private String artistId; 	// 신규 추가: 추천 요청에 필요
    private String albumImageUrl;
    private String uri;   // Web Playback SDK 재생에 쓰이는 spotify:track:xxxx 형식
}