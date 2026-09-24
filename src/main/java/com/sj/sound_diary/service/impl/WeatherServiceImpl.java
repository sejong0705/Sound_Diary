package com.sj.sound_diary.service.impl;

import com.sj.sound_diary.dto.WeatherDto;
import com.sj.sound_diary.service.WeatherService;
import com.sj.sound_diary.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    @Value("${openweather.api-key}")
    private String apiKey;

    @Value("${openweather.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    @Override
    public WeatherDto getWeather(double lat, double lon) {
        String url = String.format(
            "%s?lat=%f&lon=%f&appid=%s&units=metric&lang=kr",
            baseUrl, lat, lon, apiKey
        );

        log.info("OpenWeatherMap 요청 URL: {}", url.replace(apiKey, "****"));

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> weatherList = JsonUtils.field(response, "weather");
        Map<String, Object> weather = weatherList.get(0);
        Map<String, Object> main = JsonUtils.field(response, "main");
        Number temp = JsonUtils.field(main, "temp");

        return WeatherDto.builder()
                .description(JsonUtils.field(weather, "description"))
                .icon(JsonUtils.field(weather, "icon"))
                .temp(temp.doubleValue())
                .build();
    }
}