package com.dojangkok.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class BookmarkId implements Serializable {
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "property_post_id")
    private Long propertyPostId;
}
