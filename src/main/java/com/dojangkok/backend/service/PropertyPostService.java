package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.common.util.CursorPaginationUtil;
import com.dojangkok.backend.common.util.FileAssetValidator;
import com.dojangkok.backend.domain.*;
import com.dojangkok.backend.domain.enums.PostStatus;
import com.dojangkok.backend.dto.propertypost.*;
import com.dojangkok.backend.mapper.PropertyPostMapper;
import com.dojangkok.backend.mq.DataEventProducer;
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
    private final FileAssetRepository fileAssetRepository;
    private final DataEventProducer dataEventProducer;


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

    @Transactional(readOnly = true)
    public PropertyPostResponseDto getPropertyPostDetail(Long memberId, Long propertyPostId) {
        PropertyPost post = propertyPostRepository.findByIdIncludingDeleted(propertyPostId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_NOT_FOUND));
        Member member = post.getMember();

        if (post.getDeletedAt() != null) {
            throw new GeneralException(Code.PROPERTY_POST_DELETED);
        }

        if (post.isHidden() && !post.getMember().getId().equals(memberId)) {
            throw new GeneralException(Code.PROPERTY_POST_HIDDEN);
        }

        boolean isBookmarked = bookmarkRepository.existsById(new BookmarkId(memberId, propertyPostId));

        List<PropertyPostImageDto> images = getPropertyPostImages(post.getId());
        return propertyPostMapper.toPropertyPostResponseDto(member, post, images, isBookmarked);
    }

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

        long totalCount = bookmarkRepository.countBookmarkedPostsByMemberId(memberId);
        return buildListResponseAllBookmarked(posts, totalCount);
    }

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

    @Transactional(readOnly = true)
    public PropertyPostListResponseDto getCompletedPostList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<PropertyPost> posts;

        if (cursor == null || cursor.isEmpty()) {
            posts = propertyPostRepository.findAllCompletedByMemberId(memberId, pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            posts = propertyPostRepository.findAllCompletedByMemberIdWithCursor(memberId, cursorId, pageable);
        }

        long totalCount = propertyPostRepository.countAllCompletedByMemberId(memberId);
        return buildListResponse(memberId, posts, totalCount);
    }

    @Transactional(readOnly = true)
    public PropertyPostListResponseDto getTradingPostList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<PropertyPost> posts;

        if (cursor == null || cursor.isEmpty()) {
            posts = propertyPostRepository.findAllTradingByMemberId(memberId, pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            posts = propertyPostRepository.findAllTradingByMemberIdWithCursor(memberId, cursorId, pageable);
        }

        long totalCount = propertyPostRepository.countAllTradingByMemberId(memberId);
        return buildListResponse(memberId, posts, totalCount);
    }

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

        // 면적 검증
        if (requestDto.getExclusiveAreaM2() != null) {
            validateArea(requestDto.getExclusiveAreaM2());
        }

        // 층수 검증
        if (requestDto.getFloor() != null) {
            validateFloor(requestDto.getFloor());
        }

        // EasyContract 연결 변경
        EasyContract easyContract = null;
        Boolean isVerified = null;
        if (requestDto.getEasyContractId() != null) {
            easyContract = easyContractRepository.findByIdAndNotDeleted(requestDto.getEasyContractId())
                    .orElseThrow(() -> new GeneralException(Code.EASY_CONTRACT_NOT_FOUND));
            isVerified = true;
        }

        post.update(requestDto.getTitle(), requestDto.getAddressMain(), requestDto.getAddressDetail(),
                requestDto.getPriceMain(), requestDto.getPriceMonthly(), requestDto.getContent(),
                requestDto.getPropertyType(), requestDto.getRentType(),
                requestDto.getExclusiveAreaM2(), requestDto.getIsBasement(), requestDto.getFloor(), requestDto.getMaintenanceFee(),
                easyContract, isVerified);

        log.info("PropertyPost updated: id={}, memberId={}", propertyPostId, memberId);

        // 채팅 서버 캐시 무효화 이벤트 발행
        String thumbnailUrl = getPropertyPostImages(post.getId()).stream()
                .findFirst().map(PropertyPostImageDto::getPresignedUrl).orElse(null);
        dataEventProducer.publishPropertyUpdated(
                propertyPostId, post.getTitle(), thumbnailUrl, post.getDealStatus().name());

        List<PropertyPostImageDto> images = getPropertyPostImages(post.getId());
        return propertyPostMapper.toPropertyPostResponseDto(member, post, images, false);
    }

    @Transactional
    public PropertyPostFileAttachResponseDto attachFiles(Long memberId, Long propertyPostId,
                                                         PropertyPostFileAttachRequestDto requestDto) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);

        List<Long> fileAssetIds = requestDto.getItems().stream()
                .map(PropertyPostFileAttachItemDto::getFileAssetId)
                .toList();

        Map<Long, FileAsset> fileAssetMap = fileAssetValidator.validateAndGetFileAssets(fileAssetIds);

        // 기존 첨부 파일이 있으면 모두 삭제 (soft delete + 첨부 정보 제거)
        List<PropertyPostFile> existingFiles = propertyPostFileRepository.findAllByPropertyPostIdWithFileAsset(propertyPostId);
        if (!existingFiles.isEmpty()) {
            for (PropertyPostFile existingFile : existingFiles) {
                existingFile.getFileAsset().softDelete();
            }
            propertyPostFileRepository.deleteAll(existingFiles);
            log.info("Existing files removed from PropertyPost: propertyPostId={}, count={}", propertyPostId, existingFiles.size());
        }

        // 새 파일 첨부
        List<PropertyPostFile> newFiles = new ArrayList<>();
        int sortOrder = 0;

        for (Long fileAssetId : fileAssetIds) {
            FileAsset fileAsset = fileAssetMap.get(fileAssetId);
            sortOrder++;
            boolean isPrimary = sortOrder == 1;
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

    @Transactional
    public void deletePropertyPostFile(Long memberId, Long fileAssetId) {
        FileAsset fileAsset = getFileAssetWithOwnerCheck(memberId, fileAssetId);

        // 게시글에 첨부된 파일인지 확인
        Optional<PropertyPostFile> propertyPostFileOpt = propertyPostFileRepository.findByFileAssetId(fileAssetId);

        if (propertyPostFileOpt.isPresent()) {
            PropertyPostFile propertyPostFile = propertyPostFileOpt.get();
            Long propertyPostId = propertyPostFile.getPropertyPost().getId();
            boolean wasPrimary = propertyPostFile.isPrimary();

            // 첨부 정보 삭제
            propertyPostFileRepository.delete(propertyPostFile);

            // is_primary였던 이미지가 삭제되면 다음 순번 이미지를 primary로 변경
            if (wasPrimary) {
                propertyPostFileRepository.findNextPrimaryCandidate(propertyPostId, propertyPostFile.getId())
                        .ifPresent(PropertyPostFile::markAsPrimary);
            }

            log.info("PropertyPostFile detached and deleted: fileAssetId={}, propertyPostId={}, wasPrimary={}", fileAssetId, propertyPostId, wasPrimary);
        }

        fileAsset.softDelete();
        log.info("FileAsset soft deleted: fileAssetId={}", fileAssetId);
    }

    @Transactional
    public PropertyPostDealStatusUpdateResponseDto updateDealStatus(Long memberId, Long propertyPostId,
                                                                    PropertyPostDealStatusUpdateRequestDto requestDto) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);

        if (requestDto.getDealStatus() == null) {
            throw new GeneralException(Code.DEAL_STATUS_INVALID);
        }

        post.changeDealStatus(requestDto.getDealStatus());

        log.info("PropertyPost deal status changed: id={}, dealStatus={}", propertyPostId, requestDto.getDealStatus());

        // 채팅 서버 캐시 무효화 이벤트 발행
        dataEventProducer.publishPropertyUpdated(
                propertyPostId, post.getTitle(), null, post.getDealStatus().name());

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

    @Transactional
    public void deletePropertyPost(Long memberId, Long propertyPostId) {
        PropertyPost post = getPropertyPostWithOwnerCheck(memberId, propertyPostId);
        post.softDelete();

        log.info("PropertyPost soft deleted: id={}, memberId={}", propertyPostId, memberId);

        // 채팅 서버 캐시 무효화 이벤트 발행
        dataEventProducer.publishPropertyDeleted(propertyPostId);
    }

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

    private PropertyPost getPropertyPostWithOwnerCheck(Long memberId, Long propertyPostId) {
        PropertyPost post = propertyPostRepository.findByIdAndNotDeleted(propertyPostId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_NOT_FOUND));

        if (!post.getMember().getId().equals(memberId)) {
            throw new GeneralException(Code.PROPERTY_POST_FORBIDDEN);
        }

        return post;
    }

    private FileAsset getFileAssetWithOwnerCheck(Long memberId, Long fileAssetId) {
        FileAsset fileAsset = fileAssetRepository.findById(fileAssetId)
                .orElseThrow(() -> new GeneralException(Code.FILE_NOT_FOUND));

        if (fileAsset.getDeletedAt() != null) {
            throw new GeneralException(Code.FILE_NOT_FOUND);
        }

        // PropertyPostFile로 연결된 게시글이 있으면 소유자 확인
        propertyPostFileRepository.findByFileAssetId(fileAssetId)
                .ifPresent(ppf -> {
                    if (!ppf.getPropertyPost().getMember().getId().equals(memberId)) {
                        throw new GeneralException(Code.PROPERTY_POST_FORBIDDEN);
                    }
                });

        return fileAsset;
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
        // 보증금 범위 검증
        if (dto.getDepositMin() != null && dto.getDepositMax() != null
                && dto.getDepositMin() > dto.getDepositMax()) {
            throw new GeneralException(Code.PRICE_RANGE_INVALID);
        }
        // 매매가 범위 검증
        if (dto.getSalePriceMin() != null && dto.getSalePriceMax() != null
                && dto.getSalePriceMin() > dto.getSalePriceMax()) {
            throw new GeneralException(Code.PRICE_RANGE_INVALID);
        }
        // 월세 범위 검증
        if (dto.getPriceMonthlyMin() != null && dto.getPriceMonthlyMax() != null
                && dto.getPriceMonthlyMin() > dto.getPriceMonthlyMax()) {
            throw new GeneralException(Code.PRICE_RANGE_INVALID);
        }
    }

}
