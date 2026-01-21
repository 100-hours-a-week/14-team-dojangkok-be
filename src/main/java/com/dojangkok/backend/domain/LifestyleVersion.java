package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "lifestyle_version",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lifestyle_version_lifestyle_id_version_no",
                        columnNames = {"lifestyle_id", "version_no"}
                )
        },
        indexes = {
                @Index(name = "idx_lifestyle_version_lifestyle_id", columnList = "lifestyle_id")
        }
)
public class LifestyleVersion extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lifestyle_id", nullable = false)
    private Lifestyle lifestyle;

    @Builder
    private LifestyleVersion(int versionNo, Lifestyle lifestyle) {
        this.versionNo = versionNo;
        this.lifestyle = lifestyle;
    }

    public static LifestyleVersion createLifestyleVersion(Lifestyle lifestyle, int versionNo) {
        return LifestyleVersion.builder()
                .lifestyle(lifestyle)
                .versionNo(versionNo)
                .build();
    }
}
