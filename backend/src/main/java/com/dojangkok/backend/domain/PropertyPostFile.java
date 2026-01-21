package com.dojangkok.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "property_post_file",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_property_post_file_post_id_sort_order",
                        columnNames = {"property_post_id", "sort_order"}
                )
        }
)
public class PropertyPostFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_post_id", nullable = false)
    private PropertyPost propertyPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_asset_id", nullable = false)
    private FileAsset fileAsset;

    @Builder
    private PropertyPostFile(int sortOrder, boolean isPrimary, PropertyPost propertyPost, FileAsset fileAsset) {
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
        this.propertyPost = propertyPost;
        this.fileAsset = fileAsset;
    }

    public static PropertyPostFile createPropertyPostFile(FileAsset fileAsset, int sortOrder, boolean isPrimary, PropertyPost propertyPost) {
        return PropertyPostFile.builder()
                .sortOrder(sortOrder)
                .isPrimary(isPrimary)
                .propertyPost(propertyPost)
                .fileAsset(fileAsset)
                .build();
    }
}

