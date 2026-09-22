package com.sj.sound_diary.service;

public interface LocationService {

    // 위도/경도 → "서울 강남구 역삼동" 같은 동 단위 주소 반환
    String getDongName(double lat, double lon);
}