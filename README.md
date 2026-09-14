# 🎵 Sound Diary (감성 음악 일기장)

> **"오늘의 날씨, 그 순간 머문 장소, 그리고 귓가를 맴돌던 노래를 한 페이지에."**  
> 위치(GPS)와 실시간 기상 정보, 그리고 스포티파이 음악을 함께 바인딩하여 기록하는 감성 일기 웹 애플리케이션입니다.

<br>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-2.7.18-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Oracle_DB-21c_XE-F80000?style=for-the-badge&logo=oracle&logoColor=white" />
  <img src="https://img.shields.io/badge/MyBatis-2.3.2-black?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Spotify-API-1DB954?style=for-the-badge&logo=spotify&logoColor=white" />
</p>

---

## 📌 1. Project Overview

* **진행 기간:** 2026.09/14 - 09/29 (2주간 진행)
* **개발 인원:** 1인 (개인 프로젝트)
* **주요 목표:**
  * 레거시 라이브러리와 호환성을 고려한 안정적인 Spring Boot 2.7 환경 구축
  * 위치 기반 기상 데이터와 음원 메타데이터를 결합한 통합 일기 CRUD 구현
  * 30초 미리듣기 재생 플레이어 및 회전 턴테이블 비주얼 UI 연동

---

## 🛠 2. Tech Stack

| 분류 | 기술 스택 | 상세 설명 |
| :--- | :--- | :--- |
| **Language** | Java 17 | LTS 버전 적용 |
| **Framework** | Spring Boot 2.7.18 | Spring 5.3.x 및 javax 호환 유지 |
| **Data Access** | MyBatis 2.3.2, HikariCP | SQL 매퍼 및 커넥션 풀 관리 |
| **Database** | Oracle Database 21c XE | XEPDB1 플러그형 컨테이너 DB 연동 |
| **DB Driver** | OJDBC8 (21.9.0.0) | 21c 최적화 드라이버 |
| **External API** | OpenWeatherMap API, Spotify Web API | 날씨 및 음원 메타데이터 통신 |
| **Frontend** | HTML5, CSS3, Vanilla JS, Bootstrap 5.3, Thymeleaf | 반응형 UI 및 오디오 제어 |
| **Build & Tool** | Maven, STS4, Git / GitHub | 형상 관리 및 빌드 |

---

## 🏛 3. System Architecture & UI Layout

```text
[ Browser (Geolocation API) ]
       ↓ 위도/경도
[ OpenWeatherMap API ] ──→ 실시간 기온/아이콘 수집
       ↓
[ Spotify Web API ] ───→ 트랙 검색 (미리듣기 URL, 앨범아트 수집)
       ↓
[ Spring Boot Controller & MyBatis ]
       ↓
[ Oracle 21c Database (SOUND_DIARY) ]
