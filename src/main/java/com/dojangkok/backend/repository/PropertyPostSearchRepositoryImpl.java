package com.dojangkok.backend.repository;

import com.dojangkok.backend.common.util.CursorPaginationUtil;
import com.dojangkok.backend.domain.PropertyPost;
import com.dojangkok.backend.domain.QPropertyPost;
import com.dojangkok.backend.domain.enums.DealStatus;
import com.dojangkok.backend.domain.enums.PostStatus;
import com.dojangkok.backend.domain.enums.PropertyPostSort;
import com.dojangkok.backend.dto.propertypost.PropertyPostSearchRequestDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PropertyPostSearchRepositoryImpl implements PropertyPostSearchRepository {

    private final JPAQueryFactory queryFactory;

    private static final QPropertyPost pp = QPropertyPost.propertyPost;

    @Override
    public List<PropertyPost> search(PropertyPostSearchRequestDto request, String cursor, int pageSize) {
        BooleanBuilder builder = buildSearchCondition(request);

        // 커서 조건
        if (cursor != null && !cursor.isBlank()) {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            builder.and(pp.id.lt(cursorId));
        }

        // 정렬
        OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifiers(request.getSort());

        return queryFactory
                .selectFrom(pp)
                .where(builder)
                .orderBy(orderSpecifiers)
                .limit(pageSize + 1)
                .fetch();
    }

    @Override
    public long searchCount(PropertyPostSearchRequestDto request) {
        BooleanBuilder builder = buildSearchCondition(request);

        Long count = queryFactory
                .select(pp.count())
                .from(pp)
                .where(builder)
                .fetchOne();

        return count != null ? count : 0L;
    }

    // ==================== Private Helper ====================

    private BooleanBuilder buildSearchCondition(PropertyPostSearchRequestDto request) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건 (항상 적용)
        builder.and(pp.deletedAt.isNull());
        builder.and(pp.isHidden.isFalse());
        builder.and(pp.postStatus.eq(PostStatus.ACTIVE));
        builder.and(pp.dealStatus.eq(DealStatus.TRADING));

        // 키워드 검색 (searchText = title + addressMain)
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            builder.and(pp.searchText.containsIgnoreCase(request.getKeyword()));
        }

        // 매물 유형 (다중 선택)
        if (request.getPropertyType() != null && !request.getPropertyType().isEmpty()) {
            builder.and(pp.propertyType.in(request.getPropertyType()));
        }

        // 거래 유형 (다중 선택)
        if (request.getRentType() != null && !request.getRentType().isEmpty()) {
            builder.and(pp.rentType.in(request.getRentType()));
        }

        // 보증금/매매가 범위
        if (request.getPriceMainMin() != null) {
            builder.and(pp.priceMain.goe(request.getPriceMainMin()));
        }
        if (request.getPriceMainMax() != null) {
            builder.and(pp.priceMain.loe(request.getPriceMainMax()));
        }

        // 월세 상한
        if (request.getPriceMonthlyMax() != null) {
            builder.and(pp.priceMonthly.loe(request.getPriceMonthlyMax()));
        }

        return builder;
    }

    private OrderSpecifier<?>[] buildOrderSpecifiers(PropertyPostSort sort) {
        if (sort == null) {
            sort = PropertyPostSort.LATEST;
        }

        return switch (sort) {
            case PRICE_ASC -> new OrderSpecifier[]{pp.priceMain.asc(), pp.id.desc()};
            case PRICE_DESC -> new OrderSpecifier[]{pp.priceMain.desc(), pp.id.desc()};
            default -> new OrderSpecifier[]{pp.createdAt.desc(), pp.id.desc()};
        };
    }
}
