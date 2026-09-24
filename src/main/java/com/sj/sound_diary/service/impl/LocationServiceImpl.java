package com.sj.sound_diary.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sj.sound_diary.service.LocationService;
import com.sj.sound_diary.util.JsonUtils;
import com.sj.sound_diary.util.RestTemplateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;

    private static final String KAKAO_URL =
        "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=%f&y=%f";

    @Override
    public String getDongName(double lat, double lon) {
        String url = String.format(KAKAO_URL, lon, lat); // 카카오는 x=경도, y=위도 순서 주의

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = RestTemplateUtils.get(restTemplate, url, entity, Map.class);

            List<Map<String, Object>> documents = JsonUtils.field(response, "documents");
            if (documents.isEmpty()) return null;

            Map<String, Object> address = JsonUtils.field(documents.get(0), "address");

            String region2 = JsonUtils.field(address, "region_2depth_name"); // 구
            String region3 = JsonUtils.field(address, "region_3depth_name"); // 동

            return region2 + " " + region3;
        } catch (Exception e) {
            log.warn("카카오 역지오코딩 실패: {}", e.getMessage());
            return null;
        }
    }
}