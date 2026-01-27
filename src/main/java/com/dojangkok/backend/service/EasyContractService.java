package com.dojangkok.backend.service;

import com.dojangkok.backend.client.AiServiceClient;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.common.util.CursorPaginationUtil;
import com.dojangkok.backend.common.util.FileAssetValidator;
import com.dojangkok.backend.common.util.PaginationResult;
import com.dojangkok.backend.domain.EasyContract;
import com.dojangkok.backend.domain.EasyContractFile;
import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.dto.checklist.EasyContractFileDto;
import com.dojangkok.backend.dto.easycontract.*;
import com.dojangkok.backend.mapper.EasyContractMapper;
import com.dojangkok.backend.repository.EasyContractFileRepository;
import com.dojangkok.backend.repository.EasyContractRepository;
import com.dojangkok.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EasyContractService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final String TITLE_PREFIX = "쉬운 계약서 #";

    private final EasyContractRepository easyContractRepository;
    private final EasyContractFileRepository easyContractFileRepository;
    private final MemberRepository memberRepository;
    private final FileAssetValidator fileAssetValidator;
    private final S3Service s3Service;
    private final EasyContractMapper easyContractMapper;
    private final AiServiceClient aiServiceClient;

    @Transactional
    public EasyContractCreateResponseDto createEasyContract(Long memberId, EasyContractFileRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));
        EasyContract easyContract = EasyContract.createEasyContract(member, null, null);

        // 파일 검증 및 조회
        Map<Long, FileAsset> fileAssetMap = fileAssetValidator.validateAndGetFileAssets(requestDto.getFileAssetIds());

        // AI 요청용 DTO 생성
        List<EasyContractFileDto> fileDtos = requestDto.getFileAssetIds().stream()
                .map(fileAssetId -> {
                    FileAsset fileAsset = fileAssetMap.get(fileAssetId);
                    String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());
                    String fileName = extractFileName(fileAsset.getFileKey());
                    return EasyContractFileDto.builder()
                            .url(presignedUrl)
                            .fileName(fileName)
                            .fileType(fileAsset.getFileType())
                            .build();
                })
                .toList();

        EasyContractGenerateRequestDto generateRequestDto = EasyContractGenerateRequestDto.builder()
                .files(fileDtos)
                .build();

        // AI 서비스에 쉬운 계약서 생성 요청
        String content = aiServiceClient.requestEasyContractGeneration(generateRequestDto, easyContract.getId());

        // COMPLETED 상태인 계약서 개수 조회 후 넘버링
        String title = createTitle(memberId);

        easyContract.updateContent(title, content);
        easyContractRepository.save(easyContract);

        log.info("EasyContract created: id={}, memberId={}, title={}", easyContract.getId(), memberId, title);

        return easyContractMapper.toEasyContractCreateResponseDto(easyContract);
    }

    @Transactional(readOnly = true)
    public EasyContractListResponseDto getEasyContractList(Long memberId, String cursor) {
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE + 1);
        List<EasyContract> easyContracts;

        if (cursor == null || cursor.isEmpty()) {
            easyContracts = easyContractRepository.findAllByMemberIdAndNotDeleted(memberId, pageable);
        } else {
            Long cursorId = CursorPaginationUtil.decodeCursor(cursor);
            easyContracts = easyContractRepository.findAllByMemberIdAndNotDeletedWithCursor(memberId, cursorId, pageable);
        }

        PaginationResult<EasyContract> paginationResult = CursorPaginationUtil.paginate(
                easyContracts, DEFAULT_PAGE_SIZE, EasyContract::getId);

        List<EasyContractListItemDto> items = paginationResult.getItems().stream()
                .map(easyContractMapper::toEasyContractListItemDto)
                .toList();

        return easyContractMapper.toEasyContractListResponseDto(
                items, DEFAULT_PAGE_SIZE, paginationResult.isHasNext(), paginationResult.getNextCursor());
    }

    @Transactional(readOnly = true)
    public EasyContractDetailResponseDto getEasyContractDetail(Long memberId, Long easyContractId) {
        EasyContract easyContract = getEasyContractWithAccessCheck(memberId, easyContractId);
        return easyContractMapper.toEasyContractDetailResponseDto(easyContract);
    }

    @Transactional
    public void attachFiles(Long memberId, Long easyContractId,
                            EasyContractFileRequestDto easyContractFileRequestDto) {
        EasyContract easyContract = getEasyContractWithAccessCheck(memberId, easyContractId);

        List<Long> fileAssetIds = easyContractFileRequestDto.getFileAssetIds();

        attachFilesToEasyContract(easyContract, fileAssetIds);
        log.info("Files attached to EasyContract: easyContractId={}, count={}", easyContractId, fileAssetIds.size());
    }

    @Transactional(readOnly = true)
    public EasyContractAssetListResponseDto getEasyContractAssets(Long memberId, Long easyContractId) {
        getEasyContractWithAccessCheck(memberId, easyContractId);
        List<EasyContractFile> easyContractFiles = easyContractFileRepository
                .findAllByEasyContractIdWithFileAsset(easyContractId);

        List<EasyContractAssetItemDto> fileAssetList = easyContractFiles.stream()
                .map(ecf -> {
                    String presignedUrl = s3Service.generatePresignedDownloadUrl(ecf.getFileAsset().getFileKey());
                    return easyContractMapper.toEasyContractAssetItemDto(ecf, presignedUrl);
                })
                .toList();

        return easyContractMapper.toEasyContractAssetListResponseDto(fileAssetList);
    }

    @Transactional
    public EasyContractUpdateResponseDto updateEasyContractTitle(Long memberId, Long easyContractId,
                                                                  EasyContractUpdateRequestDto requestDto) {
        validateTitle(requestDto.getTitle());

        EasyContract easyContract = getEasyContractWithAccessCheck(memberId, easyContractId);
        easyContract.updateTitle(requestDto.getTitle());

        log.info("EasyContract title updated: id={}, newTitle={}", easyContractId, requestDto.getTitle());

        return easyContractMapper.toEasyContractUpdateResponseDto(easyContract);
    }

    @Transactional
    public void deleteEasyContract(Long memberId, Long easyContractId) {
        EasyContract easyContract = getEasyContractWithAccessCheck(memberId, easyContractId);

        // 연결된 EasyContractFile hard delete
        easyContractFileRepository.deleteAllByEasyContractId(easyContractId);

        // EasyContract soft delete
        easyContract.softDelete();

        log.info("EasyContract deleted: id={}, memberId={}", easyContractId, memberId);
    }

    private String createTitle(Long memberId) {
        int completedCount = easyContractRepository.countCompletedByMemberId(memberId);
        return TITLE_PREFIX + (completedCount + 1);
    }

    private void attachFilesToEasyContract(EasyContract easyContract, List<Long> fileAssetIds) {
        if (fileAssetIds == null || fileAssetIds.isEmpty()) {
            return;
        }

        Map<Long, FileAsset> fileAssetMap = fileAssetValidator.validateAndGetFileAssets(fileAssetIds);

        // 현재 최대 sortOrder 조회
        int maxSortOrder = easyContractFileRepository
                .findMaxSortOrderByEasyContractId(easyContract.getId())
                .orElse(0);

        List<EasyContractFile> easyContractFiles = new ArrayList<>();
        int sortOrder = maxSortOrder;

        for (Long fileAssetId : fileAssetIds) {
            FileAsset fileAsset = fileAssetMap.get(fileAssetId);
            sortOrder++;
            EasyContractFile easyContractFile = EasyContractFile.createEasyContractFile(easyContract, fileAsset, sortOrder);
            easyContractFiles.add(easyContractFile);
        }

        easyContractFileRepository.saveAll(easyContractFiles);
    }

    private EasyContract getEasyContractWithAccessCheck(Long memberId, Long easyContractId) {
        EasyContract easyContract = easyContractRepository.findByIdAndNotDeleted(easyContractId)
                .orElseThrow(() -> new GeneralException(Code.EASY_CONTRACT_NOT_FOUND));

        if (!easyContract.getMember().getId().equals(memberId)) {
            throw new GeneralException(Code.EASY_CONTRACT_ACCESS_DENIED);
        }

        return easyContract;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new GeneralException(Code.EASY_CONTRACT_TITLE_EMPTY);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new GeneralException(Code.EASY_CONTRACT_TITLE_TOO_LONG);
        }
    }

    private String extractFileName(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return "";
        }
        int lastSlashIndex = fileKey.lastIndexOf('/');
        return lastSlashIndex >= 0 ? fileKey.substring(lastSlashIndex + 1) : fileKey;
    }
}
