package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.Bookmark;
import com.dojangkok.backend.domain.BookmarkId;
import com.dojangkok.backend.domain.PropertyPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {

    void deleteAllByMemberId(Long memberId);

    boolean existsById(BookmarkId bookmarkId);

    @Query("SELECT b.propertyPost FROM Bookmark b " +
            "WHERE b.member.id = :memberId " +
            "AND b.propertyPost.deletedAt IS NULL " +
            "ORDER BY b.createdAt DESC, b.propertyPost.id DESC")
    List<PropertyPost> findBookmarkedPostsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT b.propertyPost FROM Bookmark b " +
            "WHERE b.member.id = :memberId " +
            "AND b.propertyPost.deletedAt IS NULL " +
            "AND b.propertyPost.id < :cursorId " +
            "ORDER BY b.createdAt DESC, b.propertyPost.id DESC")
    List<PropertyPost> findBookmarkedPostsByMemberIdWithCursor(@Param("memberId") Long memberId, @Param("cursorId") Long cursorId, Pageable pageable);
}
