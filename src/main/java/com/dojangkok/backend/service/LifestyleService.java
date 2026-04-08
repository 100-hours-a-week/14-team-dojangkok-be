package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.Lifestyle;
import com.dojangkok.backend.domain.LifestyleItem;
import com.dojangkok.backend.domain.LifestyleVersion;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.dojangkok.backend.dto.lifestyle.LifestyleRequestDto;
import com.dojangkok.backend.dto.lifestyle.LifestyleResponseDto;
import com.dojangkok.backend.event.ChecklistTemplateCreatedEvent;
import com.dojangkok.backend.mapper.LifestyleMapper;
import com.dojangkok.backend.repository.LifestyleItemRepository;
import com.dojangkok.backend.repository.LifestyleRepository;
import com.dojangkok.backend.repository.LifestyleVersionRepository;
import com.dojangkok.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifestyleService {

    private final MemberRepository memberRepository;
    private final LifestyleRepository lifestyleRepository;
    private final LifestyleVersionRepository lifestyleVersionRepository;
    private final LifestyleItemRepository lifestyleItemRepository;
    private final LifestyleMapper lifestyleMapper;
    private final ChecklistTemplateService checklistTemplateService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LifestyleResponseDto createLifestyle(Long memberId, LifestyleRequestDto lifestyleRequestDto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        Lifestyle lifestyle = lifestyleRepository.findByMemberId(memberId)
                .orElse(null);

        if (lifestyle == null) {
            lifestyle = createNewLifestyle(member);
            member.updateOnboardingStatus(OnboardingStatus.COMPLETE);
        }

        int nextVersionNo = getNextVersionNo(lifestyle.getId());
        LifestyleVersion lifestyleVersion = LifestyleVersion.createLifestyleVersion(lifestyle, nextVersionNo);
        lifestyleVersionRepository.save(lifestyleVersion);

        List<String> lifestyleItems = Optional.ofNullable(lifestyleRequestDto.getLifestyleItems())
                .orElse(List.of());
        List<LifestyleItem> lifestyleItemList = List.of();
        if (!lifestyleItems.isEmpty()) {
            lifestyleItemList = createLifestyleItems(lifestyleItems, lifestyleVersion);
            lifestyleItemRepository.saveAll(lifestyleItemList);
        }
        lifestyle.updateCurrentVersion(lifestyleVersion);

        // 같은 트랜잭션에서 ChecklistTemplate + OutboxEvent 저장
        Long outboxEventId = checklistTemplateService.prepareChecklistGeneration(
                memberId, lifestyleVersion.getId(), lifestyleItems);

        // 이벤트 발행 - 트랜잭션 커밋 후 비동기로 MQ 발행
        eventPublisher.publishEvent(new ChecklistTemplateCreatedEvent(outboxEventId));

        log.info("Lifestyle created with checklist preparation: memberId={}, lifestyleVersionId={}, outboxEventId={}",
                memberId, lifestyleVersion.getId(), outboxEventId);

        return lifestyleMapper.toLifestyleResponseDto(member, lifestyleItemList);
    }

    @Transactional(readOnly = true)
    public LifestyleResponseDto getLifestyle(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        Optional<Lifestyle> lifestyle = lifestyleRepository.findByMemberId(memberId);

        if (lifestyle.isEmpty() || lifestyle.get().getCurrentVersion() == null) {
            return lifestyleMapper.toEmptyResponse(member);
        }

        List<LifestyleItem> lifestyleItems = lifestyleItemRepository
                .findAllByLifestyleVersionId(lifestyle.get().getCurrentVersion().getId());

        return lifestyleMapper.toLifestyleResponseDto(member, lifestyleItems);
    }

    private Lifestyle createNewLifestyle(Member member) {
        Lifestyle lifestyle = Lifestyle.createLifestyle(member, null);
        return lifestyleRepository.save(lifestyle);
    }

    private int getNextVersionNo(Long lifestyleId) {
        return lifestyleVersionRepository.findTopByLifestyleIdOrderByVersionNoDesc(lifestyleId)
                .map(version -> version.getVersionNo() + 1)
                .orElse(1);
    }

    private List<LifestyleItem> createLifestyleItems(List<String> lifestyleItems, LifestyleVersion lifestyleVersion) {
        return lifestyleItems.stream()
                .map(content -> LifestyleItem.createLifestyleItem(content, lifestyleVersion))
                .toList();
    }
}
