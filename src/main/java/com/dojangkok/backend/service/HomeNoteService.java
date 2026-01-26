package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.common.util.FileAssetValidator;
import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.HomeNote;
import com.dojangkok.backend.domain.HomeNoteFile;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.dto.homenote.*;
import com.dojangkok.backend.mapper.HomeNoteMapper;
import com.dojangkok.backend.repository.FileAssetRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeNoteService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PREVIEW_IMAGES = 10;
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_ATTACH_ITEMS = 100;

    private final HomeNoteRepository homeNoteRepository;
    private final HomeNoteFileRepository homeNoteFileRepository;
    private final MemberRepository memberRepository;
    private final FileAssetRepository fileAssetRepository;
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
        var checklistResponseDto = checklistService.initializeChecklist(member, homeNote);

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

        List<HomeNoteListItemDto> items = homeNotes.stream()
                .map(this::toHomeNoteListItemDto)
                .toList();

        String nextCursor = hasNext && !homeNotes.isEmpty()
                ? encodeCursor(homeNotes.get(homeNotes.size() - 1).getId())
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
                ? encodeCursor(homeNoteFiles.get(homeNoteFiles.size() - 1).getId())
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
        HomeNote homeNote = getHomeNoteWithAccessCheck(memberId, homeNoteId);

        HomeNoteFile homeNoteFile = homeNoteFileRepository.findById(fileId)
                .orElseThrow(() -> new GeneralException(Code.HOME_NOTE_FILE_NOT_FOUND));

        if (!homeNoteFile.getHomeNote().getId().equals(homeNoteId)) {
            throw new GeneralException(Code.HOME_NOTE_FILE_RELATION_CONFLICT);
        }

        homeNoteFileRepository.delete(homeNoteFile);

        log.info("HomeNoteFile deleted: fileId={}, homeNoteId={}", fileId, homeNoteId);
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

    private HomeNoteListItemDto toHomeNoteListItemDto(HomeNote homeNote) {
        int fileCount = homeNoteFileRepository.countByHomeNoteId(homeNote.getId());

        Pageable pageable = PageRequest.of(0, MAX_PREVIEW_IMAGES);
        List<HomeNoteFile> previewFiles = homeNoteFileRepository.findTop10ByHomeNoteIdWithFileAsset(
                homeNote.getId(), pageable);

        List<PreviewImageDto> previewImages = previewFiles.stream()
                .map(hnf -> {
                    String presignedUrl = s3Service.generatePresignedDownloadUrl(hnf.getFileAsset().getFileKey());
                    return homeNoteMapper.toPreviewImageDto(hnf.getFileAsset().getId(), presignedUrl);
                })
                .toList();

        return homeNoteMapper.toHomeNoteListItemDto(homeNote, fileCount, previewImages);
    }

    private HomeNoteFileItemDto toHomeNoteFileItemDto(HomeNoteFile homeNoteFile) {
        String presignedUrl = s3Service.generatePresignedDownloadUrl(homeNoteFile.getFileAsset().getFileKey());
        return homeNoteMapper.toHomeNoteFileItemDto(homeNoteFile, presignedUrl);
    }

    private String encodeCursor(Long id) {
        String cursorData = String.format("{\"offset\":%d}", id);
        return Base64.getEncoder().encodeToString(cursorData.getBytes(StandardCharsets.UTF_8));
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
