package com.dojangkok.backend.service;

import com.dojangkok.backend.client.AiServiceClient;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.ChecklistTemplate;
import com.dojangkok.backend.domain.ChecklistTemplateItem;
import com.dojangkok.backend.domain.LifestyleVersion;
import com.dojangkok.backend.domain.enums.ChecklistStatus;
import com.dojangkok.backend.dto.checklist.ChecklistGenerateRequestDto;
import com.dojangkok.backend.event.LifestyleCreatedEvent;
import com.dojangkok.backend.mapper.ChecklistTemplateMapper;
import com.dojangkok.backend.repository.ChecklistTemplateItemRepository;
import com.dojangkok.backend.repository.ChecklistTemplateRepository;
import com.dojangkok.backend.repository.LifestyleVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ChecklistTemplateItemRepository checklistTemplateItemRepository;
    private final LifestyleVersionRepository lifestyleVersionRepository;
    private final AiServiceClient aiServiceClient;
    private final ChecklistTemplateMapper checklistTemplateMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLifestyleCreatedEvent(LifestyleCreatedEvent event) {
        log.info("Received LifestyleCreatedEvent for lifestyleVersionId: {}", event.lifestyleVersionId());
        generateChecklistTemplate(event.lifestyleVersionId(), event.lifestyleItems());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateChecklistTemplate(Long lifestyleVersionId, List<String> lifestyleItems) {
        try {
            // 1. PROCESSING 상태로 ChecklistTemplate 생성
            LifestyleVersion lifestyleVersion = lifestyleVersionRepository.findById(lifestyleVersionId)
                    .orElseThrow(() -> new GeneralException(Code.LIFESTYLE_VERSION_NOT_FOUND));

            ChecklistTemplate checklistTemplate = createTemplateWithStatus(lifestyleVersion, ChecklistStatus.PROCESSING);

            // 2. FastAPI에 비동기 요청 (결과는 콜백으로 받음)
//            ChecklistGenerateRequestDto request = checklistTemplateMapper.toChecklistGenerateRequestDto(lifestyleItems);
            ChecklistGenerateRequestDto request = checklistTemplateMapper.toEmptyChecklistGenerateRequestDto();

            aiServiceClient.requestChecklistGeneration(request, checklistTemplate.getId());

            log.info("Checklist generation requested for lifestyleVersionId: {}", lifestyleVersionId);

        } catch (Exception e) {
            log.error("Failed to request checklist generation for lifestyleVersionId: {}", lifestyleVersionId, e);
            handleGenerationFailure(lifestyleVersionId);
        }
    }

    private ChecklistTemplate createTemplateWithStatus(LifestyleVersion lifestyleVersion, ChecklistStatus checklistStatus) {
        ChecklistTemplate checklistTemplate = ChecklistTemplate.createChecklistTemplate(lifestyleVersion, checklistStatus);
        return checklistTemplateRepository.save(checklistTemplate);
    }

    private void saveChecklistTemplateItems(ChecklistTemplate checklistTemplate, List<String> checklistItems) {
        List<ChecklistTemplateItem> items = checklistItems.stream()
                .map(content -> ChecklistTemplateItem.createChecklistTemplateItem(content, checklistTemplate))
                .toList();
        checklistTemplateItemRepository.saveAll(items);
    }

    private void updateTemplateStatus(ChecklistTemplate checklistTemplate, ChecklistStatus checklistStatus) {
        checklistTemplate.updateStatus(checklistStatus);
        checklistTemplateRepository.save(checklistTemplate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGenerationFailure(Long lifestyleVersionId) {
        checklistTemplateRepository.findByLifestyleVersionId(lifestyleVersionId)
                .ifPresent(template -> {
                    template.updateStatus(ChecklistStatus.FAILED);
                    checklistTemplateRepository.save(template);
                });
    }

    @Transactional
    public void completeChecklistGeneration(Long templateId, List<String> checklists) {
        ChecklistTemplate template = checklistTemplateRepository.findById(templateId)
                .orElseThrow(() -> new GeneralException(Code.CHECKLIST_TEMPLATE_NOT_FOUND));

        if (checklists != null && !checklists.isEmpty()) {
            saveChecklistTemplateItems(template, checklists);
            updateTemplateStatus(template, ChecklistStatus.COMPLETED);
            log.info("Checklist template generation completed for templateId: {}", templateId);
        } else {
            updateTemplateStatus(template, ChecklistStatus.FAILED);
            log.warn("Checklist template generation failed - empty checklists for templateId: {}", templateId);
        }
    }
}
