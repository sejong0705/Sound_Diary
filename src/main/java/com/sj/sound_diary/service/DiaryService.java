package com.sj.sound_diary.service;

import java.util.List;

import com.sj.sound_diary.dto.DiaryDto;
import com.sj.sound_diary.dto.TrackRankingDto;

public interface DiaryService {

    void createDiary(DiaryDto diary, Long memberId);
    
    List<DiaryDto> getDiaryListPaged(Long memberId, int offset, int limit);
    
    DiaryDto getDiaryDetail(Long diaryId);

    void updateDiary(Long diaryId, DiaryDto diary);

    void deleteDiary(Long diaryId);
    
    List<DiaryDto> getPublicDiaryListPaged(int offset, int limit);
    //내 다이어리 개수
    int getDiaryCount(Long memberId);
    //광장 전체 다이어리 개수
    int getPublicDiaryCount();
    
    List<TrackRankingDto> getTopTracks();
}