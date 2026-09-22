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
public class WeatherDto {

    private String description;   // 예: "맑음"
    private String icon;          // 예: "01d"
    private double temp;          // 예: 25.8

}