package com.sj.sound_diary.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sj.sound_diary.dto.DiaryDto;
import com.sj.sound_diary.dto.TrackRankingDto;

@Mapper
public interface DiaryMapper {

    void insertDiary(DiaryDto diary);
    
    List<DiaryDto> selectDiaryListPaged(@Param("memberId") Long memberId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    DiaryDto selectDiaryById(Long diaryId);

    // 반환값: 수정된 행 수 (본인 글이 아니면 0)
    int updateDiary(DiaryDto diary);

    // 반환값: 삭제된 행 수 (본인 글이 아니면 0)
    int deleteDiary(@Param("diaryId") Long diaryId, @Param("memberId") Long memberId);
    //감성 광장용 페이징 목록
    List<DiaryDto> selectPublicDiaryListPaged(@Param("offset") int offset, @Param("limit") int limit);
    //내 일기 전체 개수
    int countDiaryByMemberId(Long memberId);
    //감성 광장 공개 게시물 전체 개수
    int countPublicDiary();
    //오늘의 인기곡
    List<TrackRankingDto> selectTopTracks();
}