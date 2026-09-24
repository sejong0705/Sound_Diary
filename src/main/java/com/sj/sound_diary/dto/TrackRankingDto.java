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
public class TrackRankingDto {
    private String trackName;
    private String artistName;
    private String albumImageUrl;
    private String spotifyTrackUri;
    private int cnt;
}
