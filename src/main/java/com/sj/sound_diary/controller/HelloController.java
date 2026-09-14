package com.sj.sound_diary.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HelloController {

    private final DataSource dataSource;

    HelloController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 1. 단순 Hello World 확인용
    @GetMapping("/")
    public String hello() {
        return "Hello, World! Sound Diary 서버가 정상 작동 중입니다.";
    }

    // 2. 브라우저에서 DB 및 HikariCP 연결 상태 실시간 확인용
    @GetMapping("/api/check-db")
    public Map<String, Object> checkDb() {
        Map<String, Object> response = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            response.put("status", "SUCCESS");
            response.put("message", "Oracle DB & HikariCP 연결 성공!");
            response.put("poolClass", dataSource.getClass().getName());
            response.put("databaseProduct", conn.getMetaData().getDatabaseProductVersion());
            response.put("user", conn.getMetaData().getUserName());

            // 오라클 서버 현재 시간 조회
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT SYSDATE FROM DUAL");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    response.put("oracleServerTime", rs.getString(1));
                }
            }
        } catch (Exception e) {
            response.put("status", "FAIL");
            response.put("errorMessage", e.getMessage());
        }

        return response;
    }
}