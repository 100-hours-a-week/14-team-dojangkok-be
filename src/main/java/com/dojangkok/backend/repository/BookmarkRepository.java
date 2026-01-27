package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.Bookmark;
import com.dojangkok.backend.domain.BookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {

    void deleteAllByMemberId(Long memberId);
}
