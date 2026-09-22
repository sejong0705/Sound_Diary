package com.sj.sound_diary.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.sj.sound_diary.dto.TrackDto;
import com.sj.sound_diary.service.SpotifyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {

    private final RestTemplate restTemplate;

    @Override
    public List<TrackDto> searchTracks(String query, String accessToken) {
        String url = UriComponentsBuilder.fromUriString("https://api.spotify.com/v1/search")
                .queryParam("q", query)
                .queryParam("type", "track")
                .queryParam("limit", "10")
                .build()
                .toUriString();

        return executeTrackSearch(url, accessToken);
    }

    @Override
    public TrackDto getRecommendation(String artistId, String excludeTrackUri, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 1단계: 이 아티스트의 앨범/싱글 목록 조회 (최대 10개)
        String albumUrl = "https://api.spotify.com/v1/artists/" + artistId
                + "/albums?include_groups=album,single&limit=10";

        Map<String, Object> albumResponse = restTemplate.exchange(albumUrl, HttpMethod.GET, entity, Map.class).getBody();
        List<Map<String, Object>> albums = (List<Map<String, Object>>) albumResponse.get("items");

        if (albums == null || albums.isEmpty()) {
            return null;
        }

        // 2단계: 앨범 중 하나를 무작위로 선택
        Map<String, Object> randomAlbum = albums.get(new Random().nextInt(albums.size()));
        String albumId = (String) randomAlbum.get("id");

        List<Map<String, Object>> albumImages = (List<Map<String, Object>>) randomAlbum.get("images");
        String albumImageUrl = (albumImages == null || albumImages.isEmpty())
                ? null : (String) albumImages.get(0).get("url");

        // 3단계: 그 앨범의 트랙 목록 조회
        String tracksUrl = "https://api.spotify.com/v1/albums/" + albumId + "/tracks?limit=20";
        Map<String, Object> trackResponse = restTemplate.exchange(tracksUrl, HttpMethod.GET, entity, Map.class).getBody();
        List<Map<String, Object>> items = (List<Map<String, Object>>) trackResponse.get("items");

        if (items == null || items.isEmpty()) {
            return null;
        }

        List<TrackDto> candidates = new ArrayList<>();
        for (Map<String, Object> item : items) {
            List<Map<String, Object>> artists = (List<Map<String, Object>>) item.get("artists");

            candidates.add(TrackDto.builder()
                    .name((String) item.get("name"))
                    .artistName((String) artists.get(0).get("name"))
                    .artistId((String) artists.get(0).get("id"))
                    .albumImageUrl(albumImageUrl)   // 앨범 트랙 API엔 이미지가 없어서 앨범 정보에서 가져온 값 사용
                    .uri((String) item.get("uri"))
                    .build());
        }

        // 방금 재생한 곡 제외
        candidates.removeIf(t -> t.getUri().equals(excludeTrackUri));

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(new Random().nextInt(candidates.size()));
    }
    
    // 검색 API 호출 + TrackDto 변환 (공통 로직)
    private List<TrackDto> executeTrackSearch(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
        Map<String, Object> tracks = (Map<String, Object>) response.get("tracks");
        List<Map<String, Object>> items = (List<Map<String, Object>>) tracks.get("items");

        List<TrackDto> result = new ArrayList<>();
        for (Map<String, Object> item : items) {
            List<Map<String, Object>> artists = (List<Map<String, Object>>) item.get("artists");
            Map<String, Object> album = (Map<String, Object>) item.get("album");
            List<Map<String, Object>> images = (List<Map<String, Object>>) album.get("images");

            result.add(TrackDto.builder()
                    .name((String) item.get("name"))
                    .artistName((String) artists.get(0).get("name"))
                    .artistId((String) artists.get(0).get("id"))
                    .albumImageUrl(images.isEmpty() ? null : (String) images.get(0).get("url"))
                    .uri((String) item.get("uri"))
                    .build());
        }
        return result;
    }
}