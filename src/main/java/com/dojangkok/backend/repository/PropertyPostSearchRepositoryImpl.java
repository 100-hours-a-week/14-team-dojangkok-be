package com.dojangkok.backend.repository;

import com.dojangkok.backend.common.util.CursorPaginationUtil;
import com.dojangkok.backend.domain.PropertyPost;
import com.dojangkok.backend.domain.QPropertyPost;
import com.dojangkok.backend.domain.enums.DealStatus;
import com.dojangkok.backend.domain.enums.PostStatus;
import com.dojangkok.backend.dto.propertypost.PropertyPostSearchRequestDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PropertyPostSearchRepositoryImpl implements PropertyPostSearchRepository {

    private final JPAQueryFactory queryFactory;

    private static final QPropertyPost post = QPropertyPost.propertyPost;

    @Override
    public List<PropertyPost> search(PropertyPostSearchRequestDto request, int pageSize) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건 (항상 적용)
        builder.and(post.deletedAt.isNull());
        builder.and(post.isHidden.isFalse());
        builder.and(post.postStatus.eq(PostStatus.ACTIVE));
        builder.and(post.dealStatus.eq(DealStatus.TRADING));

        // 키워드 검색
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            builder.and(post.searchText.containsIgnoreCase(request.getKeyword()));
        }

        // 매물 유형 (다중 선택)
        if (request.getPropertyTypes() != null && !request.getPropertyTypes().isEmpty()) {
            builder.and(post.propertyType.in(request.getPropertyTypes()));
        }

        // 거래 유형 (다중 선택)
        if (request.getRentTypes() != null && !request.getRentTypes().isEmpty()) {
            builder.and(post.rentType.in(request.getRentTypes()));
        }

        // 보증금/매매가 범위
        if (request.getPriceMainMin() != null) {
            builder.and(post.priceMain.goe(request.getPriceMainMin()));
        }
        if (request.getPriceMainMax() != null) {
            builder.and(post.priceMain.loe(request.getPriceMainMax()));
        }

        // 월세 범위
        if (request.getPriceMonthlyMin() != null) {
            builder.and(post.priceMonthly.goe(request.getPriceMonthlyMin()));
        }
        if (request.getPriceMonthlyMax() != null) {
            builder.and(post.priceMonthly.loe(request.getPriceMonthlyMax()));
        }

        // 면적 범위
        if (request.getAreaMin() != null) {
            builder.and(post.exclusiveAreaM2.goe(request.getAreaMin()));
        }
        if (request.getAreaMax() != null) {
            builder.and(post.exclusiveAreaM2.loe(request.getAreaMax()));
        }

        // 인증 매물 여부
        if (request.getIsVerified() != null) {
            builder.and(post.isVerified.eq(request.getIsVerified()));
        }

        // 커서 페이지네이션
        if (request.getCursor() != null && !request.getCursor().isBlank()) {
            Long cursorId = CursorPaginationUtil.decodeCursor(request.getCursor());
            builder.and(post.id.lt(cursorId));
        }

        return queryFactory
                .selectFrom(post)
                .where(builder)
                .orderBy(post.createdAt.desc(), post.id.desc())
                .limit(pageSize + 1)
                .fetch();
    }
}
