package com.sj.sound_diary.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sj.sound_diary.dto.DiaryDto;
import com.sj.sound_diary.service.DiaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    
    @GetMapping
    public List<DiaryDto> getDiaryListPaged(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        return diaryService.getDiaryListPaged(memberId, offset, limit);
    }
    
    @DeleteMapping("/{id}")
    public String deleteDiary(@PathVariable("id") Long id) {
        diaryService.deleteDiary(id);
        return "success";
    }
    @GetMapping("/count")
    public int getDiaryCount(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        return diaryService.getDiaryCount(memberId);
    }
}