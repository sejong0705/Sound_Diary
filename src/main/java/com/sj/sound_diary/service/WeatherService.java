package com.sj.sound_diary.service;

import com.sj.sound_diary.dto.WeatherDto;

public interface WeatherService {

    WeatherDto getWeather(double lat, double lon);
}