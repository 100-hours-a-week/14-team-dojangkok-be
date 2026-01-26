package com.dojangkok.backend.common.util;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.repository.FileAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileAssetValidator {

    private final FileAssetRepository fileAssetRepository;

    /**
     * 파일 ID 목록에 대해 존재 여부와 COMPLETED 상태를 검증하고 Map으로 반환
     *
     * @param fileAssetIds 검증할 파일 ID 목록
     * @return 파일 ID를 키로 하는 FileAsset Map
     * @throws GeneralException 파일이 없거나 COMPLETED 상태가 아닌 경우
     */
    public Map<Long, FileAsset> validateAndGetFileAssets(List<Long> fileAssetIds) {
        if (fileAssetIds == null || fileAssetIds.isEmpty()) {
            return Map.of();
        }

        List<FileAsset> fileAssets = fileAssetRepository.findAllByIdIn(fileAssetIds);
        Map<Long, FileAsset> fileAssetMap = fileAssets.stream()
                .collect(Collectors.toMap(FileAsset::getId, fa -> fa));

        for (Long fileAssetId : fileAssetIds) {
            FileAsset fileAsset = fileAssetMap.get(fileAssetId);
            if (fileAsset == null) {
                throw new GeneralException(Code.FILE_NOT_FOUND);
            }
            if (fileAsset.getStatus() != FileAssetStatus.COMPLETED) {
                throw new GeneralException(Code.FILE_ASSET_NOT_COMPLETED);
            }
        }

        return fileAssetMap;
    }
}
