package com.dojangkok.backend.mapper;

import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.domain.PropertyPost;
import com.dojangkok.backend.domain.PropertyPostFile;
import com.dojangkok.backend.dto.member.PostWriterInfoDto;
import com.dojangkok.backend.dto.propertypost.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertyPostMapper {

    public PropertyPostResponseDto toPropertyPostResponseDto(Member member, PropertyPost post, String presignedUrl, List<PropertyPostImageDto> images) {
        String address = post.getAddressMain() + " " + post.getAddressDetail();

        return PropertyPostResponseDto.builder()
                .writer(PostWriterInfoDto.builder()
                        .memberId(member.getId())
                        .nickname(member.getNickname())
                        .profileImageUrl(presignedUrl)
                        .build())
                .propertyPostId(post.getId())
                .title(post.getTitle())
                .address(address)
                .priceMain(post.getPriceMain())
                .priceMonthly(post.getPriceMonthly())
                .content(post.getContent())
                .propertyType(post.getPropertyType())
                .rentType(post.getRentType())
                .exclusiveAreaM2(post.getExclusiveAreaM2())
                .basement(post.isBasement())
                .floor(post.getFloor())
                .maintenanceFee(post.getMaintenanceFee())
                .postStatus(post.getPostStatus())
                .dealStatus(post.getDealStatus())
                .hidden(post.isHidden())
                .verified(post.getIsVerified())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .images(images)
                .build();
    }

    public PropertyPostImageDto toPropertyPostImageDto(PropertyPostFile ppf, String presignedUrl) {
        FileAsset fileAsset = ppf.getFileAsset();
        return PropertyPostImageDto.builder()
                .fileAssetId(fileAsset.getId())
                .presignedUrl(presignedUrl)
                .isPrimary(ppf.isPrimary())
                .sortOrder(ppf.getSortOrder())
                .build();
    }

    public PropertyPostFileAttachResponseItemDto toFileAttachResponseItem(PropertyPostFile ppf) {
        FileAsset fileAsset = ppf.getFileAsset();
        return PropertyPostFileAttachResponseItemDto.builder()
                .propertyPostFileId(ppf.getId())
                .fileAssetId(fileAsset.getId())
                .fileType(fileAsset.getFileType())
                .assetStatus(fileAsset.getStatus())
                .build();
    }

    public PropertyPostDealStatusUpdateResponseDto toDealStatusUpdateResponseDto(PropertyPost post) {
        return PropertyPostDealStatusUpdateResponseDto.builder()
                .propertyPostId(post.getId())
                .dealStatus(post.getDealStatus())
                .build();
    }

    public PostVisibilityUpdateResponseDto toPostVisibilityUpdateResponseDto(PropertyPost post) {
        return PostVisibilityUpdateResponseDto.builder()
                .propertyPostId(post.getId())
                .hidden(post.isHidden())
                .build();
    }

    public PropertyPostListItemDto toPropertyPostListItemDto(PropertyPost post, PropertyPostThumbnailDto thumbnail) {
        return PropertyPostListItemDto.builder()
                .propertyPostId(post.getId())
                .title(post.getTitle())
                .addressMain(post.getAddressMain())
                .priceMain(post.getPriceMain())
                .priceMonthly(post.getPriceMonthly())
                .rentType(post.getRentType())
                .propertyType(post.getPropertyType())
                .exclusiveAreaM2(post.getExclusiveAreaM2())
                .floor(post.getFloor())
                .maintenanceFee(post.getMaintenanceFee())
                .dealStatus(post.getDealStatus())
                .postStatus(post.getPostStatus())
                .isVerified(post.getIsVerified())
                .isHidden(post.isHidden())
                .createdAt(post.getCreatedAt())
                .thumbnail(thumbnail)
                .build();
    }
}
