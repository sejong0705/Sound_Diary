package com.sj.sound_diary.service;

import java.util.List;

import com.sj.sound_diary.dto.DiaryDto;
import com.sj.sound_diary.dto.TrackRankingDto;

public interface DiaryService {

    void createDiary(DiaryDto diary, Long memberId);
    
    List<DiaryDto> getDiaryListPaged(Long memberId, int offset, int limit);
    
    // 없는 글이거나, 남의 비공개 글이면 null
    DiaryDto getDiaryDetail(Long diaryId, Long memberId);

    // 본인 글이 아니면 false
    boolean updateDiary(Long diaryId, DiaryDto diary, Long memberId);

    // 본인 글이 아니면 false
    boolean deleteDiary(Long diaryId, Long memberId);
    
    List<DiaryDto> getPublicDiaryListPaged(int offset, int limit);
    //내 다이어리 개수
    int getDiaryCount(Long memberId);
    //광장 전체 다이어리 개수
    int getPublicDiaryCount();
    
    List<TrackRankingDto> getTopTracks();
}