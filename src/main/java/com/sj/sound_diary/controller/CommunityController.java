package com.sj.sound_diary.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sj.sound_diary.dto.DiaryDto;
import com.sj.sound_diary.dto.TrackRankingDto;
import com.sj.sound_diary.service.DiaryService;
import com.sj.sound_diary.util.SessionUtils;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CommunityController {

    private final DiaryService diaryService;

    // 광장 목록 화면 (빈 뼈대, 데이터는 JS가 비동기로 불러옴)
    @GetMapping("/community/list")
    public String listPage() {
        return "community-list";
    }

    // 광장 목록 데이터 (페이징)
    @GetMapping("/api/community")
    @ResponseBody
    public List<DiaryDto> getCommunityList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        return diaryService.getPublicDiaryListPaged(offset, limit);
    }

    // 광장 상세보기 (본인 글이면 isOwner=true로 수정/삭제 버튼 노출)
    @GetMapping("/community/detail/{id}")
    public String detailPage(@PathVariable("id") Long id, Model model, HttpSession session) {
        DiaryDto diary = diaryService.getDiaryDetail(id);
        Long currentMemberId = SessionUtils.attr(session, "memberId");

        model.addAttribute("diary", diary);
        model.addAttribute("isOwner", diary.getMemberId().equals(currentMemberId));
        model.addAttribute("backUrl", "/community/list");
        return "diary-detail";
    }
    //총계 계산
    @GetMapping("/api/community/count")
    @ResponseBody
    public int getCommunityCount() {
        return diaryService.getPublicDiaryCount();
    }
    @GetMapping("/api/community/ranking")
    @ResponseBody
    public List<TrackRankingDto> getRanking() {
        return diaryService.getTopTracks();
    }
}