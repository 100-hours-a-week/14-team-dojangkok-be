package com.dojangkok.backend.mapper;

import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.HomeNote;
import com.dojangkok.backend.domain.HomeNoteFile;
import com.dojangkok.backend.dto.checklist.ChecklistResponseDto;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteItemResponseDto;
import com.dojangkok.backend.dto.homenote.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HomeNoteMapper {

    public HomeNoteCreateResponseDto toHomeNoteCreateResponseDto(HomeNote homeNote, ChecklistResponseDto checklistResponseDto) {
        return HomeNoteCreateResponseDto.builder()
                .homeNoteId(homeNote.getId())
                .title(homeNote.getTitle())
                .checklist(checklistResponseDto)
                .build();
    }

    public HomeNoteUpdateResponseDto toHomeNoteUpdateResponseDto(HomeNote homeNote) {
        return HomeNoteUpdateResponseDto.builder()
                .homeNoteId(homeNote.getId())
                .title(homeNote.getTitle())
                .build();
    }

    public HomeNoteListResponseDto toHomeNoteListResponseDto(
            List<HomeNoteListItemDto> items,
            int limit,
            boolean hasNext,
            String nextCursor
    ) {
        return HomeNoteListResponseDto.builder()
                .limit(limit)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .items(items)
                .build();
    }

    public HomeNoteListItemDto toHomeNoteListItemDto(
            HomeNote homeNote,
            int fileCount,
            List<PreviewImageDto> previewImages
    ) {
        return HomeNoteListItemDto.builder()
                .homeNoteId(homeNote.getId())
                .title(homeNote.getTitle())
                .createdAt(homeNote.getCreatedAt())
                .updatedAt(homeNote.getUpdatedAt())
                .fileCount(fileCount)
                .previewImages(previewImages)
                .build();
    }

    public PreviewImageDto toPreviewImageDto(Long fileAssetId, String presignedUrl) {
        return PreviewImageDto.builder()
                .fileAssetId(fileAssetId)
                .presignedUrl(presignedUrl)
                .build();
    }

    public HomeNoteDetailResponseDto toHomeNoteDetailResponseDto(
            HomeNoteInfoDto homeNoteInfo,
            List<HomeNoteFileItemDto> files,
            int limit,
            boolean hasNext,
            String nextCursor
    ) {
        return HomeNoteDetailResponseDto.builder()
                .homeNote(homeNoteInfo)
                .limit(limit)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .files(files)
                .build();
    }

    public HomeNoteInfoDto toHomeNoteInfoDto(HomeNote homeNote, int fileCount) {
        return HomeNoteInfoDto.builder()
                .homeNoteId(homeNote.getId())
                .title(homeNote.getTitle())
                .createdAt(homeNote.getCreatedAt())
                .updatedAt(homeNote.getUpdatedAt())
                .fileCount(fileCount)
                .build();
    }

    public HomeNoteFileItemDto toHomeNoteFileItemDto(HomeNoteFile homeNoteFile, String presignedUrl) {
        return HomeNoteFileItemDto.builder()
                .homeNoteFileId(homeNoteFile.getId())
                .fileAssetId(homeNoteFile.getFileAsset().getId())
                .sortOrder(homeNoteFile.getSortOrder())
                .createdAt(homeNoteFile.getCreatedAt())
                .presignedUrl(presignedUrl)
                .build();
    }

    public HomeNoteFileAttachResponseDto toHomeNoteFileAttachResponseDto(List<HomeNoteFileAttachItemResponseDto> items) {
        return HomeNoteFileAttachResponseDto.builder()
                .items(items)
                .build();
    }

    public HomeNoteFileAttachItemResponseDto toHomeNoteFileAttachItemResponseDto(HomeNoteFile homeNoteFile) {
        FileAsset fileAsset = homeNoteFile.getFileAsset();
        return HomeNoteFileAttachItemResponseDto.builder()
                .homeNoteFileId(homeNoteFile.getId())
                .fileAssetId(fileAsset.getId())
                .fileType(fileAsset.getFileType())
                .assetStatus(fileAsset.getStatus())
                .build();
    }

}
