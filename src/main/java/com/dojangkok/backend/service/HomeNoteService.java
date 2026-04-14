package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.common.util.CursorPaginationUtil;
import com.dojangkok.backend.common.util.FileAssetValidator;
import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.HomeNote;
import com.dojangkok.backend.domain.HomeNoteFile;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.dto.homenote.*;
import com.dojangkok.backend.mapper.HomeNoteMapper;
import com.dojangkok.backend.repository.HomeNoteFileRepository;
import com.dojangkok.backend.repository.HomeNoteRepository;
import com.dojangkok.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeNoteService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PREVIEW_IMAGES = 4;
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_ATTACH_ITEMS = 50;

    private final HomeNoteRepository homeNoteRepository;
    private final HomeNoteFileRepository homeNoteFileRepository;
    private final MemberRepository memberRepository;
    private final ChecklistService checklistService;
    private final S3Service s3Service;
    private final HomeNoteMapper homeNoteMapper;
    private final FileAssetValidator fileAssetValidator;

    @Transactional
    public HomeNoteCreateResponseDto createHomeNote(Long memberId, HomeNoteCreateRequestDto requestDto) {
        validateTitle(requestDto.getTitle());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        HomeNote homeNote = HomeNote.createHomeNote(member, requestDto.getTitle());
        homeNoteRepository.save(homeNote);

        // 체크리스트 초기화 및 응답 DTO 반환
        var checklistResponseDto = checklistService.createChecklist(member, homeNote);

        log.info("HomeNote created: id={}, memberId={}", homeNote.getId(), memberId);

        return homeNoteMapper.toHomeNoteCreateResponseDto(homeNote, checklistResponseDto);
    }

    @Transactional(readOnly = true)
    public HomeNoteListResponseDto getHomeNoteList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<HomeNote> homeNotes;

        if (cursor == null || cursor.isEmpty()) {
            homeNotes = homeNoteRepository.findAllByMemberIdAndNotDeleted(memberId, pageable);
        } else {
            Long cursorId = decodeCursor(cursor);
            homeNotes = homeNoteRepository.findAllByMemberIdAndNotDeletedWithCursor(memberId, cursorId, pageable);
        }

        boolean hasNext = homeNotes.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            homeNotes = homeNotes.subList(0, DEFAULT_PAGE_SIZE);
        }

        // N+1 방지: HomeNote별 파일 개수와 프리뷰를 한 번에 IN-batch 조회
        List<Long> homeNoteIds = homeNotes.stream().map(HomeNote::getId).toList();
        Map<Long, Integer> fileCountMap = getFileCountMap(homeNoteIds);
        Map<Long, List<PreviewImageDto>> previewImagesMap = getPreviewImagesMap(homeNoteIds);

        List<HomeNoteListItemDto> items = homeNotes.stream()
                .map(homeNote -> homeNoteMapper.toHomeNoteListItemDto(
                        homeNote,
                        fileCountMap.getOrDefault(homeNote.getId(), 0),
                        previewImagesMap.getOrDefault(homeNote.getId(), List.of())
                ))
                .toList();

        String nextCursor = hasNext && !homeNotes.isEmpty()
                ? encodeCursor(homeNotes.getLast().getId())
                : null;

        return homeNoteMapper.toHomeNoteListResponseDto(items, DEFAULT_PAGE_SIZE, hasNext, nextCursor);
    }

    @Transactional(readOnly = true)
    public HomeNoteDetailResponseDto getHomeNoteDetail(Long memberId, Long homeNoteId, String cursor) {
        HomeNote homeNote = getHomeNoteWithAccessCheck(memberId, homeNoteId);

        int fileCount = homeNoteFileRepository.countByHomeNoteId(homeNoteId);
        HomeNoteInfoDto homeNoteInfo = homeNoteMapper.toHomeNoteInfoDto(homeNote, fileCount);

        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<HomeNoteFile> homeNoteFiles;

        if (cursor == null || cursor.isEmpty()) {
            homeNoteFiles = homeNoteFileRepository.findAllByHomeNoteIdWithFileAsset(homeNoteId, pageable);
        } else {
            Long cursorId = decodeCursor(cursor);
            homeNoteFiles = homeNoteFileRepository.findAllByHomeNoteIdWithFileAssetAndCursor(homeNoteId, cursorId, pageable);
        }

        boolean hasNext = homeNoteFiles.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            homeNoteFiles = homeNoteFiles.subList(0, DEFAULT_PAGE_SIZE);
        }

        List<HomeNoteFileItemDto> items = homeNoteFiles.stream()
                .map(this::toHomeNoteFileItemDto)
                .toList();

        String nextCursor = hasNext && !homeNoteFiles.isEmpty()
                ? encodeCursor(homeNoteFiles.getLast().getId())
                : null;

        return homeNoteMapper.toHomeNoteDetailResponseDto(homeNoteInfo, items, DEFAULT_PAGE_SIZE, hasNext, nextCursor);
    }

    @Transactional
    public HomeNoteUpdateResponseDto updateHomeNoteTitle(Long memberId, Long homeNoteId, HomeNoteUpdateRequestDto requestDto) {
        validateTitle(requestDto.getTitle());

        HomeNote homeNote = getHomeNoteWithAccessCheck(memberId, homeNoteId);
        homeNote.changeTitle(requestDto.getTitle());

        log.info("HomeNote title updated: id={}, newTitle={}", homeNoteId, requestDto.getTitle());

        return homeNoteMapper.toHomeNoteUpdateResponseDto(homeNote);
    }

    @Transactional
    public void deleteHomeNote(Long memberId, Long homeNoteId) {
        HomeNote homeNote = getHomeNoteWithAccessCheck(memberId, homeNoteId);
        homeNote.softDelete();

        log.info("HomeNote soft deleted: id={}, memberId={}", homeNoteId, memberId);
    }

    @Transactional
    public HomeNoteFileAttachResponseDto attachFiles(Long memberId, Long homeNoteId, HomeNoteFileAttachRequestDto requestDto) {
        HomeNote homeNote = getHomeNoteWithAccessCheck(memberId, homeNoteId);

        List<Long> fileAssetIds = requestDto.getFiles().stream()
                .map(HomeNoteFileAttachItemRequestDto::getFileAssetId)
                .toList();

        // 최대 첨부 개수 검증
        int currentFileCount = homeNoteFileRepository.countByHomeNoteId(homeNoteId);
        if (currentFileCount + fileAssetIds.size() > MAX_ATTACH_ITEMS) {
            throw new GeneralException(Code.HOME_NOTE_ITEMS_TOO_MANY);
        }

        Map<Long, FileAsset> fileAssetMap = fileAssetValidator.validateAndGetFileAssets(fileAssetIds);

        // 현재 최대 sortOrder 조회
        int maxSortOrder = homeNoteFileRepository.findMaxSortOrderByHomeNoteId(homeNoteId).orElse(0);

        List<HomeNoteFile> homeNoteFiles = new ArrayList<>();
        int sortOrder = maxSortOrder;

        for (Long fileAssetId : fileAssetIds) {
            FileAsset fileAsset = fileAssetMap.get(fileAssetId);
            sortOrder++;
            HomeNoteFile homeNoteFile = HomeNoteFile.createHomeNoteFile(homeNote, fileAsset, sortOrder);
            homeNoteFiles.add(homeNoteFile);
        }

        List<HomeNoteFile> savedFiles = homeNoteFileRepository.saveAll(homeNoteFiles);

        List<HomeNoteFileAttachItemResponseDto> responseItems = savedFiles.stream()
                .map(homeNoteMapper::toHomeNoteFileAttachItemResponseDto)
                .toList();

        log.info("Files attached to HomeNote: homeNoteId={}, count={}", homeNoteId, savedFiles.size());

        return homeNoteMapper.toHomeNoteFileAttachResponseDto(responseItems);
    }

    @Transactional
    public void deleteHomeNoteFile(Long memberId, Long homeNoteId, Long fileId) {
        getHomeNoteWithAccessCheck(memberId, homeNoteId);

        HomeNoteFile homeNoteFile = homeNoteFileRepository.findByFileAssetIdAndHomeNoteId(fileId, homeNoteId)
                .orElseThrow(() -> new GeneralException(Code.HOME_NOTE_FILE_NOT_FOUND));

        // FileAsset soft delete (배치 작업에서 S3 파일 삭제 예정)
        FileAsset fileAsset = homeNoteFile.getFileAsset();
        fileAsset.softDelete();

        // HomeNoteFile hard delete
        homeNoteFileRepository.delete(homeNoteFile);

        log.info("HomeNoteFile deleted: fileId={}, homeNoteId={}, fileAssetId={}", fileId, homeNoteId, fileAsset.getId());
    }

    private HomeNote getHomeNoteWithAccessCheck(Long memberId, Long homeNoteId) {
        HomeNote homeNote = homeNoteRepository.findByIdAndNotDeleted(homeNoteId)
                .orElseThrow(() -> new GeneralException(Code.HOME_NOTE_NOT_FOUND));

        if (!homeNote.getMember().getId().equals(memberId)) {
            throw new GeneralException(Code.HOME_NOTE_ACCESS_DENIED);
        }

        return homeNote;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new GeneralException(Code.HOME_NOTE_TITLE_EMPTY);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new GeneralException(Code.HOME_NOTE_TITLE_TOO_LONG);
        }
    }

    private Map<Long, Integer> getFileCountMap(List<Long> homeNoteIds) {
        if (homeNoteIds.isEmpty()) {
            return Map.of();
        }
        return homeNoteFileRepository.countByHomeNoteIdIn(homeNoteIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    private Map<Long, List<PreviewImageDto>> getPreviewImagesMap(List<Long> homeNoteIds) {
        if (homeNoteIds.isEmpty()) {
            return Map.of();
        }

        List<HomeNoteFile> allFiles =
                homeNoteFileRepository.findAllByHomeNoteIdInWithFileAsset(homeNoteIds);

        // homeNoteId별로 그룹핑 (정렬은 쿼리 ORDER BY로 보장됨)
        Map<Long, List<HomeNoteFile>> filesByHomeNoteId = allFiles.stream()
                .collect(Collectors.groupingBy(hnf -> hnf.getHomeNote().getId()));

        Map<Long, List<PreviewImageDto>> result = new HashMap<>();
        for (Map.Entry<Long, List<HomeNoteFile>> entry : filesByHomeNoteId.entrySet()) {
            List<PreviewImageDto> previews = entry.getValue().stream()
                    .limit(MAX_PREVIEW_IMAGES)
                    .map(hnf -> {
                        String presignedUrl = "";
//                        String presignedUrl = s3Service.generatePresignedDownloadUrl(hnf.getFileAsset().getFileKey());
                        return homeNoteMapper.toPreviewImageDto(hnf.getFileAsset().getId(), presignedUrl);
                    })
                    .toList();
            result.put(entry.getKey(), previews);
        }
        return result;
    }

    private HomeNoteFileItemDto toHomeNoteFileItemDto(HomeNoteFile homeNoteFile) {
        String presignedUrl = s3Service.generatePresignedDownloadUrl(homeNoteFile.getFileAsset().getFileKey());
        return homeNoteMapper.toHomeNoteFileItemDto(homeNoteFile, presignedUrl);
    }

    private String encodeCursor(Long id) {
        return CursorPaginationUtil.encodeCursor(id);
    }

    private Long decodeCursor(String cursor) {
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            // {"offset":10} 형태에서 숫자 추출
            String numberStr = decoded.replaceAll("[^0-9]", "");
            return Long.parseLong(numberStr);
        } catch (Exception e) {
            throw new GeneralException(Code.BAD_REQUEST);
        }
    }
}
