package com.sj.sound_diary.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sj.sound_diary.dto.WeatherDto;
import com.sj.sound_diary.service.LocationService;
import com.sj.sound_diary.service.WeatherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    private final LocationService locationService;
    
    @GetMapping("/api/weather")
    public WeatherDto getWeather(
        @RequestParam double lat,
        @RequestParam double lon
    ) {
        return weatherService.getWeather(lat, lon);
    }
    
    @GetMapping("/api/location")
    public Map<String, String> getLocation(
        @RequestParam double lat,
        @RequestParam double lon
    ) {
        String dongName = locationService.getDongName(lat, lon);
        return Map.of("locationName", dongName != null ? dongName : "위치 확인 불가");
    }
}