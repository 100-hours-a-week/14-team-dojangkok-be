package com.dojangkok.backend.controller;

import com.dojangkok.backend.auth.jwt.CurrentMemberId;
import com.dojangkok.backend.common.dto.DataResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteRequestDto;
import com.dojangkok.backend.dto.propertypost.*;
import com.dojangkok.backend.service.PropertyPostFileUploadService;
import com.dojangkok.backend.service.PropertyPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/property-posts")
public class PropertyPostController {

    private final PropertyPostService propertyPostService;
    private final PropertyPostFileUploadService propertyPostFileUploadService;

    // 매물 게시글 검색/필터링 - 목록
    @PostMapping("/searches")
    public DataResponseDto<PropertyPostSearchResponseDto> searchPropertyPosts(@CurrentMemberId Long memberId, @RequestParam(required = false) String cursor,
                                                                              @Valid @RequestBody PropertyPostSearchRequestDto requestDto) {
        PropertyPostSearchResponseDto responseDto = propertyPostService.searchPropertyPosts(memberId, requestDto, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "매물 검색에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 검색/필터링 - 개수만
    @PostMapping("/searches/count")
    public DataResponseDto<PropertyPostSearchCountResponseDto> countPropertyPosts(@Valid @RequestBody PropertyPostSearchRequestDto requestDto) {
        PropertyPostSearchCountResponseDto responseDto = propertyPostService.countPropertyPosts(requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "매물 개수 조회에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 목록 조회
    @GetMapping
    public DataResponseDto<PropertyPostListResponseDto> getPropertyPostList(@CurrentMemberId Long memberId,
                                                                            @RequestParam(required = false) String cursor) {
        PropertyPostListResponseDto responseDto = propertyPostService.getPropertyPostList(memberId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "매물 목록 조회에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 상세 조회
    @GetMapping("/{propertyPostId}")
    public DataResponseDto<PropertyPostResponseDto> getPropertyPostDetail(@CurrentMemberId Long memberId,
                                                                          @PathVariable Long propertyPostId) {
        PropertyPostResponseDto responseDto = propertyPostService.getPropertyPostDetail(memberId, propertyPostId);
        return new DataResponseDto<>(Code.SUCCESS, "매물 상세 조회에 성공하였습니다.", responseDto);
    }

    // 스크랩 매물 게시글 목록 조회
    @GetMapping("/bookmarks")
    public DataResponseDto<PropertyPostListResponseDto> getBookmarkedPostList(@CurrentMemberId Long memberId,
                                                                              @RequestParam(required = false) String cursor) {
        PropertyPostListResponseDto responseDto = propertyPostService.getBookmarkedPostList(memberId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "스크랩 매물 목록 조회에 성공하였습니다.", responseDto);
    }

    // 숨긴 매물 게시글 목록 조회
    @GetMapping("/hidden")
    public DataResponseDto<PropertyPostListResponseDto> getHiddenPostList(@CurrentMemberId Long memberId,
                                                                          @RequestParam(required = false) String cursor) {
        PropertyPostListResponseDto responseDto = propertyPostService.getHiddenPostList(memberId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "숨긴 매물 목록 조회에 성공하였습니다.", responseDto);
    }

    // 거래 완료 게시글 목록 조회
    @GetMapping("/completed")
    public DataResponseDto<PropertyPostListResponseDto> getCompletedPostList(@CurrentMemberId Long memberId,
                                                                             @RequestParam(required = false) String cursor) {
        PropertyPostListResponseDto responseDto = propertyPostService.getCompletedPostList(memberId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "거래 완료 매물 목록 조회에 성공하였습니다.", responseDto);
    }

    // 거래 중 게시글 목록 조회
    @GetMapping("/trading")
    public DataResponseDto<PropertyPostListResponseDto> getTradingPostList(@CurrentMemberId Long memberId,
                                                                           @RequestParam(required = false) String cursor) {
        PropertyPostListResponseDto responseDto = propertyPostService.getTradingPostList(memberId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "거래 중 매물 목록 조회에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 등록
    @PostMapping
    public DataResponseDto<PropertyPostCreateResponseDto> createPropertyPost(@CurrentMemberId Long memberId,
                                                                              @Valid @RequestBody PropertyPostCreateRequestDto requestDto) {
        PropertyPostCreateResponseDto responseDto = propertyPostService.createPropertyPost(memberId, requestDto);
        return new DataResponseDto<>(Code.CREATED_SUCCESS, "매물 게시글 생성에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 수정
    @PatchMapping("/{propertyPostId}")
    public DataResponseDto<PropertyPostResponseDto> updatePropertyPost(@CurrentMemberId Long memberId, @PathVariable Long propertyPostId,
                                                                       @RequestBody PropertyPostUpdateRequestDto requestDto) {
        PropertyPostResponseDto responseDto = propertyPostService.updatePropertyPost(memberId, propertyPostId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "매물 게시글 수정에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 파일 업로드 준비 (Presigned URL 발급)
    @PostMapping("/files/presigned-urls")
    public DataResponseDto<PropertyPostFileUploadResponseDto> generatePresignedUrls(@RequestBody @Valid PropertyPostFileUploadRequestDto requestDto) {
        PropertyPostFileUploadResponseDto responseDto = propertyPostFileUploadService.generatePresignedUrls(requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "Presigned URL 발급에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 파일 업로드 완료
    @PostMapping("/files/complete")
    public DataResponseDto<PropertyPostFileCompleteResponseDto> completeFileUpload(@Valid @RequestBody FileUploadCompleteRequestDto requestDto) {
        PropertyPostFileCompleteResponseDto responseDto = propertyPostFileUploadService.completeFileUpload(requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "파일 업로드 완료 처리에 성공하였습니다.", responseDto);
    }

    // 매물 게시글 이미지 첨부
    @PostMapping("/{propertyPostId}/files")
    public DataResponseDto<PropertyPostFileAttachResponseDto> attachFiles(@CurrentMemberId Long memberId, @PathVariable Long propertyPostId,
                                                                          @Valid @RequestBody PropertyPostFileAttachRequestDto requestDto) {
        PropertyPostFileAttachResponseDto responseDto = propertyPostService.attachFiles(memberId, propertyPostId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "매물 게시글 이미지가 성공적으로 첨부되었습니다.", responseDto);
    }

    // 매물 게시글 이미지 삭제
    @DeleteMapping("/files/{fileAssetId}")
    public ResponseEntity<Void> deletePropertyPostFile(@CurrentMemberId Long memberId, @PathVariable Long fileAssetId) {
        propertyPostService.deletePropertyPostFile(memberId, fileAssetId);
        return ResponseEntity.noContent().build();
    }

    // 매물 게시글 거래 상태 변경
    @PatchMapping("/{propertyPostId}/deal-status")
    public DataResponseDto<PropertyPostDealStatusUpdateResponseDto> updateDealStatus(@CurrentMemberId Long memberId, @PathVariable Long propertyPostId,
                                                                                     @Valid @RequestBody PropertyPostDealStatusUpdateRequestDto requestDto) {
        PropertyPostDealStatusUpdateResponseDto responseDto = propertyPostService.updateDealStatus(memberId, propertyPostId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "매물 게시글 거래 상태가 변경되었습니다.", responseDto);
    }

    // 매물 게시글 노출 상태 변경
    @PatchMapping("/{propertyPostId}/post-status")
    public DataResponseDto<PostVisibilityUpdateResponseDto> updatePostVisibility(@CurrentMemberId Long memberId, @PathVariable Long propertyPostId,
                                                                                 @Valid @RequestBody PostVisibilityUpdateRequestDto requestDto) {
        PostVisibilityUpdateResponseDto responseDto = propertyPostService.updatePostVisibility(memberId, propertyPostId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "매물 게시글 노출 상태가 변경되었습니다.", responseDto);
    }

    // 매물 게시글 삭제
    @DeleteMapping("/{propertyPostId}")
    public ResponseEntity<Void> deletePropertyPost(@CurrentMemberId Long memberId, @PathVariable Long propertyPostId) {
        propertyPostService.deletePropertyPost(memberId, propertyPostId);
        return ResponseEntity.noContent().build();
    }

    // 스크랩 추가
    @PostMapping("/{propertyPostId}/bookmarks")
    public ResponseEntity<Void> addBookmark(@CurrentMemberId Long memberId, @PathVariable Long propertyPostId) {
        propertyPostService.addBookmark(memberId, propertyPostId);
        return ResponseEntity.noContent().build();
    }

    // 스크랩 제거
    @DeleteMapping("/{propertyPostId}/bookmarks")
    public ResponseEntity<Void> removeBookmark(@CurrentMemberId Long memberId, @PathVariable Long propertyPostId) {
        propertyPostService.removeBookmark(memberId, propertyPostId);
        return ResponseEntity.noContent().build();
    }
}
