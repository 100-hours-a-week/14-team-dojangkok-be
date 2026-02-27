package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.common.util.CursorPaginationUtil;
import com.dojangkok.backend.common.util.FileAssetValidator;
import com.dojangkok.backend.common.util.PresignedUrlUtil;
import com.dojangkok.backend.domain.*;
import com.dojangkok.backend.domain.enums.PostStatus;
import com.dojangkok.backend.dto.propertypost.*;
import com.dojangkok.backend.mapper.PropertyPostMapper;
import com.dojangkok.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyPostService {

    private static final int MAX_TITLE_LENGTH = 50;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final PropertyPostRepository propertyPostRepository;
    private final PropertyPostFileRepository propertyPostFileRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final EasyContractRepository easyContractRepository;
    private final S3Service s3Service;
    private final PropertyPostMapper propertyPostMapper;
    private final FileAssetValidator fileAssetValidator;
    private final PresignedUrlUtil presignedUrlUtil;

    // ==================== 매물 게시글 검색/필터링 ====================

    @Transactional(readOnly = true)
    public PropertyPostSearchResponseDto searchPropertyPosts(Long memberId, PropertyPostSearchRequestDto requestDto, String cursor) {
        validateSearchRequest(requestDto);

        List<PropertyPost> posts = propertyPostRepository.search(requestDto, cursor, DEFAULT_PAGE_SIZE);
        long totalCount = propertyPostRepository.searchCount(requestDto);

        boolean hasNext = posts.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            posts = posts.subList(0, DEFAULT_PAGE_SIZE);
        }

        String nextCursor = hasNext && !posts.isEmpty()
                ? CursorPaginationUtil.encodeCursor(posts.getLast().getId())
                : null;

        Set<Long> bookmarkedPostIds = getBookmarkedPostIds(memberId, posts);

        List<PropertyPostListItemDto> items = posts.stream()
                .map(post -> {
                    PropertyPostThumbnailDto thumbnail = getThumbnail(post.getId());
                    return propertyPostMapper.toPropertyPostListItemDto(post, thumbnail, bookmarkedPostIds.contains(post.getId()));
                })
                .toList();

        return PropertyPostSearchResponseDto.builder()
                .totalCount(totalCount)
                .limit(DEFAULT_PAGE_SIZE)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public PropertyPostSearchCountResponseDto countPropertyPosts(PropertyPostSearchRequestDto requestDto) {
        validateSearchRequest(requestDto);
        long count = propertyPostRepository.searchCount(requestDto);
        return PropertyPostSearchCountResponseDto.builder().count(count).build();
    }

    // ==================== 매물 게시글 목록 조회 ====================
    public PropertyPostListResponseDto getPropertyPostList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<PropertyPost> posts;

        if (cursor == null || cursor.isEmpty()) {
            posts = propertyPostRepository.findAllActiveAndVisible(pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            posts = propertyPostRepository.findAllActiveAndVisibleWithCursor(cursorId, pageable);
        }

        long totalCount = propertyPostRepository.countAllActiveAndVisible();
        return buildListResponse(memberId, posts, totalCount);
    }

    // ==================== 매물 게시글 상세 조회 ====================

    @Transactional(readOnly = true)
    public PropertyPostResponseDto getPropertyPostDetail(Long memberId, Long propertyPostId) {
        PropertyPost post = propertyPostRepository.findByIdIncludingDeleted(propertyPostId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_NOT_FOUND));
        Member member = post.getMember();

        if (post.getDeletedAt() != null) {
            throw new GeneralException(Code.PROPERTY_POST_DELETED);
        }

        boolean isBookmarked = bookmarkRepository.existsById(new BookmarkId(memberId, propertyPostId));

        List<PropertyPostImageDto> images = getPropertyPostImages(post.getId());
        String presignedUrl = s3Service.generatePresignedDownloadUrl(member.getProfileImage());
        return propertyPostMapper.toPropertyPostResponseDto(member, post, presignedUrl, images, isBookmarked);
    }

    // ==================== 스크랩 매물 게시글 목록 조회 ====================

    @Transactional(readOnly = true)
    public PropertyPostListResponseDto getBookmarkedPostList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<PropertyPost> posts;

        if (cursor == null || cursor.isEmpty()) {
            posts = bookmarkRepository.findBookmarkedPostsByMemberId(memberId, pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            posts = bookmarkRepository.findBookmarkedPostsByMemberIdWithCursor(memberId, cursorId, pageable);
        }

        // 스크랩 목록이므로 전부 bookmarked=true
        long totalCount = bookmarkRepository.countBookmarkedPostsByMemberId(memberId);
        return buildListResponseAllBookmarked(posts, totalCount);
    }

    // ==================== 숨긴 매물 게시글 목록 조회 ====================

    @Transactional(readOnly = true)
    public PropertyPostListResponseDto getHiddenPostList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<PropertyPost> posts;

        if (cursor == null || cursor.isEmpty()) {
            posts = propertyPostRepository.findAllHiddenByMemberId(memberId, pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            posts = propertyPostRepository.findAllHiddenByMemberIdWithCursor(memberId, cursorId, pageable);
        }

        return buildListResponse(memberId, posts, propertyPostRepository.countAllHiddenByMemberId(memberId));
    }

    // ==================== 거래 완료 게시글 목록 조회 ====================

    @Transactional(readOnly = true)
    public PropertyPostListResponseDto getCompletedPostList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<PropertyPost> posts;

        if (cursor == null || cursor.isEmpty()) {
            posts = propertyPostRepository.findAllCompleted(pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            posts = propertyPostRepository.findAllCompletedWithCursor(cursorId, pageable);
        }

        long totalCount = propertyPostRepository.countAllCompleted();
        return buildListResponse(memberId, posts, totalCount);
    }

    // ==================== 거래 중 게시글 목록 조회 ====================

    @Transactional(readOnly = true)
    public PropertyPostListResponseDto getTradingPostList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<PropertyPost> posts;

        if (cursor == null || cursor.isEmpty()) {
            posts = propertyPostRepository.findAllTrading(pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            posts = propertyPostRepository.findAllTradingWithCursor(cursorId, pageable);
        }

        long totalCount = propertyPostRepository.countAllTrading();
        return buildListResponse(memberId, posts, totalCount);
    }

    // ==================== 매물 게시글 생성 ====================

    @Transactional
    public PropertyPostCreateResponseDto createPropertyPost(Long memberId, PropertyPostCreateRequestDto requestDto) {
        validateCreateRequest(requestDto);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        // EasyContract 연결 (선택)
        EasyContract easyContract = null;
        boolean isVerified = false;
        if (requestDto.getEasyContractId() != null) {
            easyContract = easyContractRepository.findByIdAndNotDeleted(requestDto.getEasyContractId())
                    .orElseThrow(() -> new GeneralException(Code.EASY_CONTRACT_NOT_FOUND));
            isVerified = true;
        }

        // address 직접 사용 (프론트에서 main/detail 분리하여 전달)
        String addressDetail = requestDto.getAddressDetail() != null ? requestDto.getAddressDetail() : "";

        PropertyPost post = PropertyPost.createPropertyPost(
                member, easyContract,
                requestDto.getTitle(),
                requestDto.getAddressMain(), addressDetail,
                requestDto.getPriceMain(), requestDto.getPriceMonthly(),
                requestDto.getContent(),
                requestDto.getPropertyType(), requestDto.getRentType(),
                requestDto.getExclusiveAreaM2(), requestDto.getIsBasement(),
                requestDto.getFloor(), requestDto.getMaintenanceFee(),
                isVerified
        );

        propertyPostRepository.save(post);

        log.info("PropertyPost created: id={}, memberId={}", post.getId(), memberId);

        return PropertyPostCreateResponseDto.builder()
                .propertyPostId(post.getId())
                .build();
    }

    // ==================== 매물 게시글 수정 ====================

    @Transactional
    public PropertyPostResponseDto updatePropertyPost(Long memberId, Long propertyPostId, PropertyPostUpdateRequestDto requestDto) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);
        Member member = post.getMember();

        // ACTIVE 상태에서만 수정 가능
        if (post.getPostStatus() != PostStatus.ACTIVE) {
            throw new GeneralException(Code.PROPERTY_POST_UPDATE_NOT_ALLOWED);
        }

        // 제목 검증
        if (requestDto.getTitle() != null) {
            validateTitle(requestDto.getTitle());
        }

        // 가격 검증
        if (requestDto.getPriceMain() != null) {
            validatePrice(requestDto.getPriceMain());
        }

        // EasyContract 연결 변경
        EasyContract easyContract = null;
        Boolean isVerified = null;
        if (requestDto.getEasyContractId() != null) {
            easyContract = easyContractRepository.findByIdAndNotDeleted(requestDto.getEasyContractId())
                    .orElseThrow(() -> new GeneralException(Code.EASY_CONTRACT_NOT_FOUND));
            isVerified = true;
        }

        post.update(requestDto.getTitle(), requestDto.getPriceMain(),
                requestDto.getPriceMonthly(), requestDto.getContent(),
                easyContract, isVerified);

        log.info("PropertyPost updated: id={}, memberId={}", propertyPostId, memberId);

        List<PropertyPostImageDto> images = getPropertyPostImages(post.getId());
        return propertyPostMapper.toPropertyPostResponseDto(member, post, presignedUrlUtil.generatePresignedUrlUtil(member.getProfileImage()), images, false);
    }

    // ==================== 매물 게시글 이미지 첨부 ====================

    @Transactional
    public PropertyPostFileAttachResponseDto attachFiles(Long memberId, Long propertyPostId,
                                                         PropertyPostFileAttachRequestDto requestDto) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);

        List<Long> fileAssetIds = requestDto.getItems().stream()
                .map(PropertyPostFileAttachItemDto::getFileAssetId)
                .toList();

        Map<Long, FileAsset> fileAssetMap = fileAssetValidator.validateAndGetFileAssets(fileAssetIds);

        int maxSortOrder = propertyPostFileRepository.findMaxSortOrderByPropertyPostId(propertyPostId).orElse(0);
        boolean hasPrimary = maxSortOrder > 0;

        List<PropertyPostFile> newFiles = new ArrayList<>();
        int sortOrder = maxSortOrder;

        for (Long fileAssetId : fileAssetIds) {
            FileAsset fileAsset = fileAssetMap.get(fileAssetId);
            sortOrder++;
            boolean isPrimary = !hasPrimary && sortOrder == 1;
            PropertyPostFile ppf = PropertyPostFile.createPropertyPostFile(fileAsset, sortOrder, isPrimary, post);
            newFiles.add(ppf);
        }

        List<PropertyPostFile> savedFiles = propertyPostFileRepository.saveAll(newFiles);

        List<PropertyPostFileAttachResponseItemDto> items = savedFiles.stream()
                .map(propertyPostMapper::toFileAttachResponseItem)
                .toList();

        log.info("Files attached to PropertyPost: propertyPostId={}, count={}", propertyPostId, savedFiles.size());

        return PropertyPostFileAttachResponseDto.builder().items(items).build();
    }

    // ==================== 매물 게시글 이미지 삭제 ====================

    @Transactional
    public void deletePropertyPostFile(Long memberId, Long propertyPostId, Long fileId) {
        getPropertyPostWithOwnerCheck(memberId, propertyPostId);

        PropertyPostFile ppf = propertyPostFileRepository.findByFileAssetIdAndPropertyPostId(fileId, propertyPostId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_FILE_NOT_FOUND));

        boolean wasPrimary = ppf.isPrimary();

        // is_primary였던 이미지가 삭제되면 다음 순번 이미지를 primary로 변경
        if (wasPrimary) {
            propertyPostFileRepository.findNextPrimaryCandidate(propertyPostId, ppf.getId())
                    .ifPresent(PropertyPostFile::markAsPrimary);
        }

        FileAsset fileAsset = ppf.getFileAsset();
        fileAsset.softDelete();

        propertyPostFileRepository.delete(ppf);

        log.info("PropertyPostFile deleted: fileId={}, propertyPostId={}, wasPrimary={}", fileId, propertyPostId, wasPrimary);
    }

    // ==================== 거래 상태 변경 ====================

    @Transactional
    public PropertyPostDealStatusUpdateResponseDto updateDealStatus(Long memberId, Long propertyPostId,
                                                                    PropertyPostDealStatusUpdateRequestDto requestDto) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);

        if (requestDto.getDealStatus() == null) {
            throw new GeneralException(Code.DEAL_STATUS_INVALID);
        }

        post.changeDealStatus(requestDto.getDealStatus());

        log.info("PropertyPost deal status changed: id={}, dealStatus={}", propertyPostId, requestDto.getDealStatus());

        return propertyPostMapper.toDealStatusUpdateResponseDto(post);
    }

    // ==================== 노출 상태 변경 ====================

    @Transactional
    public PostVisibilityUpdateResponseDto updatePostVisibility(Long memberId, Long propertyPostId,
                                                                 PostVisibilityUpdateRequestDto requestDto) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);

        if (requestDto.getIsHidden() == null) {
            throw new GeneralException(Code.POST_VISIBILITY_INVALID);
        }

        post.changeHidden(requestDto.getIsHidden());

        log.info("PropertyPost visibility changed: id={}, isHidden={}", propertyPostId, requestDto.getIsHidden());

        return propertyPostMapper.toPostVisibilityUpdateResponseDto(post);
    }

    // ==================== 매물 게시글 삭제 ====================

    @Transactional
    public void deletePropertyPost(Long memberId, Long propertyPostId) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);
        post.softDelete();

        log.info("PropertyPost soft deleted: id={}, memberId={}", propertyPostId, memberId);
    }

    // ==================== 스크랩 추가 ====================

    @Transactional
    public void addBookmark(Long memberId, Long propertyPostId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        PropertyPost post = propertyPostRepository.findByIdAndNotDeleted(propertyPostId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_NOT_FOUND));

        BookmarkId bookmarkId = new BookmarkId(memberId, propertyPostId);
        if (bookmarkRepository.existsById(bookmarkId)) {
            throw new GeneralException(Code.BOOKMARK_ALREADY_EXISTS);
        }

        Bookmark bookmark = Bookmark.createBookMark(member, post);
        bookmarkRepository.save(bookmark);

        log.info("Bookmark added: memberId={}, propertyPostId={}", memberId, propertyPostId);
    }

    // ==================== 스크랩 제거 ====================

    @Transactional
    public void removeBookmark(Long memberId, Long propertyPostId) {
        propertyPostRepository.findByIdAndNotDeleted(propertyPostId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_NOT_FOUND));

        BookmarkId bookmarkId = new BookmarkId(memberId, propertyPostId);
        if (!bookmarkRepository.existsById(bookmarkId)) {
            throw new GeneralException(Code.BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.deleteById(bookmarkId);

        log.info("Bookmark removed: memberId={}, propertyPostId={}", memberId, propertyPostId);
    }

    // ==================== Private Helper Methods ====================

    private PropertyPost getPropertyPostWithOwnerCheck(Long memberId, Long propertyPostId) {
        PropertyPost post = propertyPostRepository.findByIdAndNotDeleted(propertyPostId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_NOT_FOUND));

        if (!post.getMember().getId().equals(memberId)) {
            throw new GeneralException(Code.PROPERTY_POST_FORBIDDEN);
        }

        return post;
    }

    private List<PropertyPostImageDto> getPropertyPostImages(Long propertyPostId) {
        List<PropertyPostFile> files = propertyPostFileRepository.findAllByPropertyPostIdWithFileAsset(propertyPostId);
        return files.stream()
                .map(ppf -> {
                    String presignedUrl = s3Service.generatePresignedDownloadUrl(ppf.getFileAsset().getFileKey());
                    return propertyPostMapper.toPropertyPostImageDto(ppf, presignedUrl);
                })
                .toList();
    }

    private PropertyPostThumbnailDto getThumbnail(Long propertyPostId) {
        List<PropertyPostFile> files = propertyPostFileRepository.findAllByPropertyPostIdWithFileAsset(propertyPostId);
        return files.stream()
                .filter(PropertyPostFile::isPrimary)
                .findFirst()
                .or(() -> files.stream().findFirst())
                .map(ppf -> PropertyPostThumbnailDto.builder()
                        .fileAssetId(ppf.getFileAsset().getId())
                        .presignedUrl(s3Service.generatePresignedDownloadUrl(ppf.getFileAsset().getFileKey()))
                        .build())
                .orElse(null);
    }

    private PropertyPostListResponseDto buildListResponse(Long memberId, List<PropertyPost> posts, long totalCount) {
        boolean hasNext = posts.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            posts = posts.subList(0, DEFAULT_PAGE_SIZE);
        }

        String nextCursor = hasNext && !posts.isEmpty()
                ? CursorPaginationUtil.encodeCursor(posts.getLast().getId())
                : null;

        Set<Long> bookmarkedPostIds = getBookmarkedPostIds(memberId, posts);

        List<PropertyPostListItemDto> items = posts.stream()
                .map(post -> {
                    PropertyPostThumbnailDto thumbnail = getThumbnail(post.getId());
                    return propertyPostMapper.toPropertyPostListItemDto(post, thumbnail, bookmarkedPostIds.contains(post.getId()));
                })
                .toList();

        return PropertyPostListResponseDto.builder()
                .totalCount(totalCount)
                .limit(DEFAULT_PAGE_SIZE)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .propertyPostItems(items)
                .build();
    }

    private PropertyPostListResponseDto buildListResponseAllBookmarked(List<PropertyPost> posts, long totalCount) {
        boolean hasNext = posts.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            posts = posts.subList(0, DEFAULT_PAGE_SIZE);
        }

        String nextCursor = hasNext && !posts.isEmpty()
                ? CursorPaginationUtil.encodeCursor(posts.getLast().getId())
                : null;

        List<PropertyPostListItemDto> items = posts.stream()
                .map(post -> {
                    PropertyPostThumbnailDto thumbnail = getThumbnail(post.getId());
                    return propertyPostMapper.toPropertyPostListItemDto(post, thumbnail, true);
                })
                .toList();

        return PropertyPostListResponseDto.builder()
                .totalCount(totalCount)
                .limit(DEFAULT_PAGE_SIZE)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .propertyPostItems(items)
                .build();
    }

    private Set<Long> getBookmarkedPostIds(Long memberId, List<PropertyPost> posts) {
        if (posts.isEmpty()) {
            return Set.of();
        }
        List<Long> postIds = posts.stream().map(PropertyPost::getId).toList();
        return new HashSet<>(bookmarkRepository.findBookmarkedPostIdsByMemberIdAndPostIds(memberId, postIds));
    }

    private void validateCreateRequest(PropertyPostCreateRequestDto dto) {
        validateTitle(dto.getTitle());
        validatePrice(dto.getPriceMain());
        validateArea(dto.getExclusiveAreaM2());
        validateFloor(dto.getFloor());
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
            throw new GeneralException(Code.TITLE_INVALID);
        }
    }

    private void validatePrice(Long priceMain) {
        if (priceMain == null || priceMain < 0) {
            throw new GeneralException(Code.PRICE_INVALID);
        }
    }

    private void validateArea(BigDecimal area) {
        if (area == null || area.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GeneralException(Code.AREA_INVALID);
        }
    }

    private void validateFloor(BigDecimal floor) {
        if (floor == null) {
            throw new GeneralException(Code.FLOOR_INVALID);
        }
    }

    private void validateSearchRequest(PropertyPostSearchRequestDto dto) {
        // 보증금/매매가 범위 검증
        if (dto.getPriceMainMin() != null && dto.getPriceMainMax() != null
                && dto.getPriceMainMin() > dto.getPriceMainMax()) {
            throw new GeneralException(Code.PRICE_RANGE_INVALID);
        }
        // 월세 범위 검증
        if (dto.getPriceMonthlyMin() != null && dto.getPriceMonthlyMax() != null
                && dto.getPriceMonthlyMin() > dto.getPriceMonthlyMax()) {
            throw new GeneralException(Code.PRICE_RANGE_INVALID);
        }
    }

}
