package com.dojangkok.backend.service;

import com.dojangkok.backend.auth.token.RedisRefreshTokenStore;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.*;
import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.dojangkok.backend.dto.member.UpdateNicknameRequestDto;
import com.dojangkok.backend.dto.member.UpdateNicknameResponseDto;
import com.dojangkok.backend.dto.member.ProfileResponseDto;
import com.dojangkok.backend.mapper.MemberMapper;
import com.dojangkok.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Long WITHDRAWN_MEMBER_ID = -1L;

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final RedisRefreshTokenStore refreshTokenStore;
    private final WithdrawnMemberRepository withdrawnMemberRepository;
    private final SocialAuthRepository socialAuthRepository;
    private final BookmarkRepository bookmarkRepository;
    private final HomeNoteRepository homeNoteRepository;
    private final HomeNoteFileRepository homeNoteFileRepository;
    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final EasyContractRepository easyContractRepository;
    private final EasyContractFileRepository easyContractFileRepository;
    private final LifestyleRepository lifestyleRepository;
    private final LifestyleVersionRepository lifestyleVersionRepository;
    private final LifestyleItemRepository lifestyleItemRepository;
    private final PropertyPostRepository propertyPostRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ChecklistTemplateItemRepository checklistTemplateItemRepository;

    @Transactional
    public UpdateNicknameResponseDto updateNickname(Long memberId, UpdateNicknameRequestDto updateNicknameRequestDto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        if (member.getNickname() == null) {
            member.updateOnboardingStatus(OnboardingStatus.LIFESTYLE);
        }

        String nickname = updateNicknameRequestDto.getNickname();
        validateNickname(nickname);
        member.updateNickname(nickname);

        return memberMapper.toUpdateNicknameResponse(member, nickname);
    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));
        return memberMapper.toProfileResponse(member);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(Code.MEMBER_NOT_FOUND));

        // 1. Refresh Token 무효화
        refreshTokenStore.delete(memberId);

        // 2. PropertyPost의 member_id를 -1로 변경
        propertyPostRepository.updateMemberIdByMemberId(memberId, WITHDRAWN_MEMBER_ID);

        // 3. 관련 데이터 삭제
        bookmarkRepository.deleteAllByMemberId(memberId);
        deleteChecklists(memberId);
        deleteHomeNotes(memberId);
        deleteEasyContracts(memberId);
        deleteLifestyles(memberId);

        // 4. SocialAuth 삭제
        socialAuthRepository.deleteByMemberId(memberId);

        // 5. WithdrawnMember로 이동
        WithdrawnMember withdrawnMember = WithdrawnMember.createWithdrawnMember(member);
        withdrawnMemberRepository.save(withdrawnMember);

        // 6. Member 삭제
        memberRepository.delete(member);
    }

    private void deleteChecklists(Long memberId) {
        List<Checklist> checklists = checklistRepository.findAllByMemberId(memberId);
        if (!checklists.isEmpty()) {
            List<Long> checklistIds = checklists.stream()
                    .map(Checklist::getId)
                    .toList();
            checklistItemRepository.deleteAllByChecklistIdIn(checklistIds);
            checklistRepository.deleteAllByMemberId(memberId);
        }
    }

    private void deleteHomeNotes(Long memberId) {
        List<HomeNote> homeNotes = homeNoteRepository.findAllByMemberId(memberId);
        if (!homeNotes.isEmpty()) {
            List<Long> homeNoteIds = homeNotes.stream()
                    .map(HomeNote::getId)
                    .toList();
            homeNoteFileRepository.deleteAllByHomeNoteIdIn(homeNoteIds);
            homeNoteRepository.deleteAllByMemberId(memberId);
        }
    }

    private void deleteEasyContracts(Long memberId) {
        List<EasyContract> easyContracts = easyContractRepository.findAllByMemberId(memberId);
        if (!easyContracts.isEmpty()) {
            List<Long> easyContractIds = easyContracts.stream()
                    .map(EasyContract::getId)
                    .toList();
            easyContractFileRepository.deleteAllByEasyContractIdIn(easyContractIds);
            easyContractRepository.deleteAllByMemberId(memberId);
        }
    }

    private void deleteLifestyles(Long memberId) {
        lifestyleRepository.findByMemberId(memberId).ifPresent(lifestyle -> {
            Long lifestyleId = lifestyle.getId();
            
            // 1. currentVersion FK를 벌크 쿼리로 해제
            lifestyleRepository.clearCurrentVersion(lifestyleId);
            
            // 2. lifestyle_version 조회
            List<LifestyleVersion> versions = lifestyleVersionRepository.findAllByLifestyleId(lifestyleId);
            if (!versions.isEmpty()) {
                List<Long> versionIds = versions.stream()
                        .map(LifestyleVersion::getId)
                        .toList();
                
                // 3. checklist_template 조회
                List<ChecklistTemplate> templates = checklistTemplateRepository.findAllByLifestyleVersionIdIn(versionIds);
                if (!templates.isEmpty()) {
                    List<Long> templateIds = templates.stream()
                            .map(ChecklistTemplate::getId)
                            .toList();
                    
                    // 4. checklist_template_item 벌크 삭제
                    checklistTemplateItemRepository.deleteAllByChecklistTemplateIdIn(templateIds);
                    
                    // 5. checklist_template 벌크 삭제
                    checklistTemplateRepository.deleteAllByLifestyleVersionIdIn(versionIds);
                }
                
                // 6. lifestyle_item 삭제
                lifestyleItemRepository.deleteAllByLifestyleVersionIdIn(versionIds);
            }
            
            // 7. lifestyle_version 삭제
            lifestyleVersionRepository.deleteAllByLifestyleId(lifestyleId);
            
            // 8. lifestyle 삭제
            lifestyleRepository.deleteByMemberId(memberId);
        });
    }

    private void validateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new GeneralException(Code.NICKNAME_CONFLICT);
        } else if (nickname.length()>10) {
            throw new GeneralException(Code.NICKNAME_TOO_LONG);
        }
    }
}
