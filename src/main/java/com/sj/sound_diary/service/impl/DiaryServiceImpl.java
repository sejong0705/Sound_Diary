package com.sj.sound_diary.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sj.sound_diary.dto.DiaryDto;
import com.sj.sound_diary.mapper.DiaryMapper;
import com.sj.sound_diary.service.DiaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;

    // TEMP_MEMBER_ID 삭제 — 이제 컨트롤러가 세션에서 꺼내 넘겨줌

    @Override
    public void createDiary(DiaryDto diary, Long memberId) {
        if (diary.getTitle() == null || diary.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
        }
        if (diary.getContent() == null || diary.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("일기 내용은 비워둘 수 없습니다.");
        }
        diary.setMemberId(memberId);
        if (diary.getIsPublic() == null) {
            diary.setIsPublic("N");
        }
        diaryMapper.insertDiary(diary);
    }

//    @Override
//    public List<DiaryDto> getDiaryList(Long memberId) {
//        return diaryMapper.selectDiaryList(memberId);
//    }
    
    @Override
    public List<DiaryDto> getDiaryListPaged(Long memberId, int offset, int limit) {
        return diaryMapper.selectDiaryListPaged(memberId, offset, limit);
    }
    
    @Override
    public DiaryDto getDiaryDetail(Long diaryId) {
        return diaryMapper.selectDiaryById(diaryId);
    }

    @Override
    public void updateDiary(Long diaryId, DiaryDto diary) {
        if (diary.getTitle() == null || diary.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
        }
        if (diary.getContent() == null || diary.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("일기 내용은 비워둘 수 없습니다.");
        }
        diary.setDiaryId(diaryId);
        diaryMapper.updateDiary(diary);
    }

    @Override
    public void deleteDiary(Long diaryId) {
        diaryMapper.deleteDiary(diaryId);
    }
    
    @Override
    public List<DiaryDto> getPublicDiaryListPaged(int offset, int limit) {
        return diaryMapper.selectPublicDiaryListPaged(offset, limit);
    }
    
    @Override
    public int getDiaryCount(Long memberId) {
        return diaryMapper.countDiaryByMemberId(memberId);
    }

    @Override
    public int getPublicDiaryCount() {
        return diaryMapper.countPublicDiary();
    }
    @Override
    public List<Map<String, Object>> getTopTracks() {
        return diaryMapper.selectTopTracks();
    }
}