package com.sj.sound_diary.service;

import com.sj.sound_diary.dto.TrackDto;
import java.util.List;

public interface SpotifyService {
    List<TrackDto> searchTracks(String query, String accessToken);
    // 같은 아티스트의 다른 곡 하나 추천 (방금 재생한 곡 제외)
    TrackDto getRecommendation(String artistName, String excludeTrackUri, String accessToken);
}