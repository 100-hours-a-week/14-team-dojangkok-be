package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "home_note_file",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_home_note_file_home_note_id_file_asset_id",
                        columnNames = {"home_note_id", "file_asset_id"}
                ),
                @UniqueConstraint(
                        name = "uk_file_asset_id",
                        columnNames = {"file_asset_id"}
                )
        },
        indexes = {
                @Index(name = "idx_home_note_file_home_note_id_sort_order", columnList = "home_note_id, sort_order")
        }
)
public class HomeNoteFile extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_note_id", nullable = false)
    private HomeNote homeNote;

    // OneToOne 대신 ManyToOne으로 열어두고 unique로 제약
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_asset_id", nullable = false)
    private FileAsset fileAsset;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    private HomeNoteFile(HomeNote homeNote, FileAsset fileAsset, int sortOrder) {
        this.homeNote = homeNote;
        this.fileAsset = fileAsset;
        this.sortOrder = sortOrder;
    }

    public static HomeNoteFile createHomeNoteFile(HomeNote homeNote, FileAsset fileAsset, int sortOrder) {
        return HomeNoteFile.builder()
                .homeNote(homeNote)
                .fileAsset(fileAsset)
                .sortOrder(sortOrder)
                .build();
    }
}

