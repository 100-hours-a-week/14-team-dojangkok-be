package com.dojangkok.backend.common.util;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CursorPaginationUtil {

    /**
     * 커서 기반 페이지네이션 처리
     *
     * @param items       조회된 아이템 목록 (pageSize + 1개)
     * @param pageSize    페이지 크기
     * @param idExtractor 아이템에서 ID를 추출하는 함수
     * @return 페이지네이션 결과
     */
    public static <T> PaginationResult<T> paginate(List<T> items, int pageSize, Function<T, Long> idExtractor) {
        boolean hasNext = items.size() > pageSize;
        List<T> pagedItems = hasNext ? items.subList(0, pageSize) : items;

        String nextCursor = hasNext && !pagedItems.isEmpty()
                ? encodeCursor(idExtractor.apply(pagedItems.getLast()))
                : null;

        return new PaginationResult<>(pagedItems, hasNext, nextCursor);
    }

    /**
     * 커서 인코딩
     */
    public static String encodeCursor(Long id) {
        String cursorData = String.format("{\"offset\":%d}", id);
        return Base64.getEncoder().encodeToString(cursorData.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 커서 디코딩
     */
    public static Long decodeCursor(String cursor) {
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            String numberStr = decoded.replaceAll("[^0-9]", "");
            return Long.parseLong(numberStr);
        } catch (Exception e) {
            throw new GeneralException(Code.BAD_REQUEST);
        }
    }
}
