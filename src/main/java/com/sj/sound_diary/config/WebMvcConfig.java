package com.sj.sound_diary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/**")                     // 기본적으로 전체 경로 검사
                .excludePathPatterns(
                    "/api/auth/**",   // 로그인/콜백 자체는 검사 제외 (안 그러면 무한 리다이렉트)
                    "/css/**",        // 정적 리소스는 제외
                    "/js/**",
                    "/images/**"
                );
    }
}