package com.dojangkok.backend.common.util;

import com.dojangkok.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PresignedUrlUtil {

    private final S3Service s3Service;

    public String generatePresignedUrlUtil(String fileKey) {
        return s3Service.generatePresignedDownloadUrl(fileKey);
    }
}
