package com.sj.sound_diary.controller;

import com.sj.sound_diary.dto.DiaryDto;
import com.sj.sound_diary.service.DiaryService;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class DiaryFormController {

    private final DiaryService diaryService;
    
    //글쓰기 메뉴를 눌렀을 때 
    @GetMapping("/diary/write")
    public String writePage() {
        return "diary-write";
    }

    //작성 후 등록하기 버튼을 눌렀을 때
    @PostMapping("/diary/write")
    public String submitDiary(@ModelAttribute DiaryDto diaryDto, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        diaryService.createDiary(diaryDto, memberId);
        return "redirect:/diary/list";
    }
    
    //내 일기 리스트 출력 화면
    @GetMapping("/diary/list")
    public String listPage() {
        return "diary-list";
    }
    
    // 상세보기 (본인 일기 — 수정/삭제 버튼 노출)
    @GetMapping("/diary/detail/{id}")
    public String detailPage(@PathVariable("id") Long id, Model model) {
        model.addAttribute("diary", diaryService.getDiaryDetail(id));
        model.addAttribute("isOwner", true);  // 나중에 감성 광장에서 재사용 시 false로 넘기면 수정/삭제 버튼 숨겨짐
        model.addAttribute("backUrl", "/diary/list");
        return "diary-detail";
    }

    // 수정 저장
    @PostMapping("/diary/detail/{id}")
    public String updateDiary(@PathVariable("id") Long id, @ModelAttribute DiaryDto diaryDto) {
        diaryService.updateDiary(id, diaryDto);
        return "redirect:/diary/detail/" + id;
    }
    @GetMapping("/app")
    public String appShell() {
        return "app-shell";
    }
}