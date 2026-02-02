package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.domain.enums.FileType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "file_asset",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_media_asset_file_key", columnNames = "file_key")
        },
        indexes = {
                @Index(name = "idx_file_asset_status_created_at", columnList = "status, created_at")
        }
)
public class FileAsset extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // self reference (원본 -> 썸네일)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "thumbnail_id",
            foreignKey = @ForeignKey(name = "fk_file_asset_thumbnail")
    )
    private FileAsset thumbnail;

    @Column(name = "file_key", nullable = false)
    private String fileKey;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FileAssetStatus status;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    public FileAsset(Long id, FileAsset thumbnail, String fileKey, String originalFileName, FileType fileType, FileAssetStatus status,
                     String contentType, Map<String, Object> metadata, LocalDateTime deletedAt) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.fileKey = fileKey;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.status = status;
        this.contentType = contentType;
        this.metadata = metadata;
        this.deletedAt = deletedAt;
    }

    public static FileAsset createFileAsset(String fileKey, FileType fileType, String originalFileName,
                                             String contentType, Map<String, Object> metadata) {
        return FileAsset.builder()
                .fileKey(fileKey)
                .fileType(fileType)
                .originalFileName(originalFileName)
                .status(FileAssetStatus.UPLOADING)
                .contentType(contentType)
                .metadata(metadata)
                .build();
    }

    public void markCompleted() {
        this.status = FileAssetStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        this.status = FileAssetStatus.FAILED;
        this.metadata.put("failReason", reason);
    }

    public void attachThumbnail(FileAsset thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

}

