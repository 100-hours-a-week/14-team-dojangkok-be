package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.Lifestyle;
import com.dojangkok.backend.domain.LifestyleItem;
import com.dojangkok.backend.domain.LifestyleVersion;
import com.dojangkok.backend.domain.Member;
import com.dojangkok.backend.dto.lifestyle.LifestyleRequestDto;
import com.dojangkok.backend.dto.lifestyle.LifestyleResponseDto;
import com.dojangkok.backend.event.LifestyleCreatedEvent;
import com.dojangkok.backend.mapper.LifestyleMapper;
import com.dojangkok.backend.repository.LifestyleItemRepository;
import com.dojangkok.backend.repository.LifestyleRepository;
import com.dojangkok.backend.repository.LifestyleVersionRepository;
import com.dojangkok.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LifestyleService {

    private final MemberRepository memberRepository;
    private final LifestyleRepository lifestyleRepository;
    private final LifestyleVersionRepository lifestyleVersionRepository;
    private final LifestyleItemRepository lifestyleItemRepository;
    private final LifestyleMapper lifestyleMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LifestyleResponseDto createLifestyle(Long memberId, LifestyleRequestDto createLifestyleRequestDto) {

        List<String> lifestyleItems =  createLifestyleRequestDto.getLifestyleItems();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        Lifestyle lifestyle = lifestyleRepository.findByMemberId(memberId)
                .orElseGet(() -> createNewLifestyle(member));

        int nextVersionNo = getNextVersionNo(lifestyle.getId());

        LifestyleVersion lifestyleVersion = LifestyleVersion.createLifestyleVersion(lifestyle, nextVersionNo);
        lifestyleVersionRepository.save(lifestyleVersion);

        List<LifestyleItem> lifestyleItemsList = createLifestyleItems(lifestyleItems, lifestyleVersion);
        lifestyleItemRepository.saveAll(lifestyleItemsList);

        lifestyle.updateCurrentVersion(lifestyleVersion);

        // 이벤트 발행 - 트랜잭션 커밋 후 비동기로 체크리스트 생성
        eventPublisher.publishEvent(new LifestyleCreatedEvent(lifestyleVersion.getId(), lifestyleItems));

        return lifestyleMapper.toLifestyleResponseDto(memberId, lifestyleItemsList);
    }

    @Transactional(readOnly = true)
    public LifestyleResponseDto getLifestyle(Long memberId) {
        Optional<Lifestyle> lifestyle = lifestyleRepository.findByMemberId(memberId);

        if (lifestyle.isEmpty() || lifestyle.get().getCurrentVersion() == null) {
            return lifestyleMapper.toEmptyResponse(memberId);
        }

        List<LifestyleItem> lifestyleItems = lifestyleItemRepository
                .findAllByLifestyleVersionId(lifestyle.get().getCurrentVersion().getId());

        return lifestyleMapper.toLifestyleResponseDto(memberId, lifestyleItems);
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

    private List<LifestyleItem> createLifestyleItems(List<String> contents, LifestyleVersion lifestyleVersion) {
        return contents.stream()
                .map(content -> LifestyleItem.createLifestyleItem(content, lifestyleVersion))
                .toList();
    }
}
