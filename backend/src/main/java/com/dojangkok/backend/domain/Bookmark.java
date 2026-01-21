package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "bookmark",
        indexes = {
                @Index(name = "idx_bookmark_member_created_post", columnList = "member_id, created_at, property_post_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BaseCreatedTimeEntity {

    @EmbeddedId
    private BookmarkId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @MapsId("propertyPostId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_post_id", nullable = false)
    private PropertyPost propertyPost;

    @Builder
    private Bookmark(BookmarkId id, Member member, PropertyPost propertyPost) {
        this.id = id;
        this.member = member;
        this.propertyPost = propertyPost;
    }

    public static Bookmark createBookMark(Member member, PropertyPost propertyPost) {
        return Bookmark.builder()
                .id(new BookmarkId(member.getId(), propertyPost.getId()))
                .member(member)
                .propertyPost(propertyPost)
                .build();
    }
}
