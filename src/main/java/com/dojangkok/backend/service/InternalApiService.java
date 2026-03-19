package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.domain.PropertyPost;
import com.dojangkok.backend.domain.PropertyPostFile;
import com.dojangkok.backend.dto.internal.InternalPropertyResponseDto;
import com.dojangkok.backend.dto.internal.InternalUserResponseDto;
import com.dojangkok.backend.repository.MemberRepository;
import com.dojangkok.backend.repository.PropertyPostFileRepository;
import com.dojangkok.backend.repository.PropertyPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalApiService {

    private final MemberRepository memberRepository;
    private final PropertyPostRepository propertyPostRepository;
    private final PropertyPostFileRepository propertyPostFileRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public InternalUserResponseDto getUserProfile(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        return InternalUserResponseDto.builder()
                .userId(String.valueOf(member.getId()))
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImage())
                .build();
    }

    @Transactional(readOnly = true)
    public InternalPropertyResponseDto getPropertyPostInfo(Long propertyId) {
        PropertyPost post = propertyPostRepository.findByIdAndNotDeleted(propertyId)
                .orElseThrow(() -> new GeneralException(Code.PROPERTY_POST_NOT_FOUND));

        String imageUrl = getPrimaryImageUrl(propertyId);

        return InternalPropertyResponseDto.builder()
                .propertyId(String.valueOf(post.getId()))
                .title(post.getTitle())
                .imageUrl(imageUrl)
                .priceMain(post.getPriceMain())
                .priceMonthly(post.getPriceMonthly())
                .rentType(post.getRentType().name())
                .dealStatus(post.getDealStatus().name())
                .build();
    }

    private String getPrimaryImageUrl(Long propertyPostId) {
        List<PropertyPostFile> files = propertyPostFileRepository
                .findAllByPropertyPostIdWithFileAsset(propertyPostId);

        return files.stream()
                .filter(PropertyPostFile::isPrimary)
                .findFirst()
                .or(() -> files.stream().findFirst())
                .map(ppf -> s3Service.generatePresignedDownloadUrl(ppf.getFileAsset().getFileKey()))
                .orElse(null);
    }
}
