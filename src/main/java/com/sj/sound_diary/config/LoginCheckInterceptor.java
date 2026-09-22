package com.sj.sound_diary.config;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 세션에 로그인 정보(memberId)가 없으면 무조건 Spotify 로그인 화면으로 리다이렉트
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();

        if (session.getAttribute("memberId") == null) {
            response.sendRedirect("/api/auth/login");
            return false; // 여기서 요청 처리 중단
        }

        return true; // 로그인 되어있으면 그대로 진행
    }
}