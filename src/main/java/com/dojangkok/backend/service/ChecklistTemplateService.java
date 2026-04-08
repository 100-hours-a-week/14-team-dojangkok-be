package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.common.util.CorrelationIdGenerator;
import com.dojangkok.backend.domain.ChecklistTemplate;
import com.dojangkok.backend.domain.ChecklistTemplateItem;
import com.dojangkok.backend.domain.LifestyleVersion;
import com.dojangkok.backend.domain.OutboxEvent;
import com.dojangkok.backend.domain.enums.ChecklistTemplateStatus;
import com.dojangkok.backend.dto.checklist.ChecklistGenerateRequestDto;
import com.dojangkok.backend.mapper.ChecklistTemplateMapper;
import com.dojangkok.backend.mq.config.RabbitMQConfig;
import com.dojangkok.backend.mq.dto.ChecklistMqRequestDto;
import com.dojangkok.backend.repository.ChecklistTemplateItemRepository;
import com.dojangkok.backend.repository.ChecklistTemplateRepository;
import com.dojangkok.backend.repository.LifestyleVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ChecklistTemplateItemRepository checklistTemplateItemRepository;
    private final LifestyleVersionRepository lifestyleVersionRepository;
    private final ChecklistTemplateMapper checklistTemplateMapper;
    private final OutboxEventService outboxEventService;

    public Long prepareChecklistGeneration(Long memberId, Long lifestyleVersionId, List<String> lifestyleItems) {
        LifestyleVersion lifestyleVersion = lifestyleVersionRepository.findById(lifestyleVersionId)
                .orElseThrow(() -> new GeneralException(Code.LIFESTYLE_VERSION_NOT_FOUND));

        ChecklistTemplate checklistTemplate = ChecklistTemplate.createChecklistTemplate(lifestyleVersion);
        checklistTemplateRepository.save(checklistTemplate);

        String correlationId = CorrelationIdGenerator.generate();
        ChecklistGenerateRequestDto generateRequest =
                checklistTemplateMapper.toChecklistGenerateRequestDto(lifestyleItems);

        ChecklistMqRequestDto request = ChecklistMqRequestDto.builder()
                .correlationId(correlationId)
                .templateId(checklistTemplate.getId())
                .memberId(memberId)
                .keywords(generateRequest.getKeywords())
                .build();

        OutboxEvent outboxEvent = outboxEventService.saveEvent(
                "CHECKLIST_TEMPLATE",
                checklistTemplate.getId(),
                RabbitMQConfig.WAS_EXCHANGE,
                "quorum.checklist.request",
                request
        );

        log.info("Checklist generation prepared: templateId={}, correlationId={}, outboxEventId={}",
                checklistTemplate.getId(), correlationId, outboxEvent.getId());

        return outboxEvent.getId();
    }

    @Transactional
    public void completeChecklistGeneration(Long templateId, List<String> checklists) {
        ChecklistTemplate template = checklistTemplateRepository.findById(templateId)
                .orElseThrow(() -> new GeneralException(Code.CHECKLIST_TEMPLATE_NOT_FOUND));

        // 멱등성 체크: PROCESSING 상태일 때만 처리
        if (template.getChecklistTemplateStatus() != ChecklistTemplateStatus.PROCESSING) {
            log.info("Checklist already processed, skipping: templateId={}, status={}",
                    templateId, template.getChecklistTemplateStatus());
            return;
        }

        if (checklists != null && !checklists.isEmpty()) {
            saveChecklistTemplateItems(template, checklists);
            updateTemplateStatus(template, ChecklistTemplateStatus.COMPLETED);
            log.info("Checklist template generation completed for templateId: {}", templateId);
        } else {
            updateTemplateStatus(template, ChecklistTemplateStatus.FAILED);
            log.warn("Checklist template generation failed - empty checklists for templateId: {}", templateId);
        }
    }

    @Transactional
    public void handleGenerationFailure(Long lifestyleVersionId) {
        checklistTemplateRepository.findByLifestyleVersionId(lifestyleVersionId)
                .ifPresent(template -> {
                    template.updateStatus(ChecklistTemplateStatus.FAILED);
                    checklistTemplateRepository.save(template);
                });
    }

    private void saveChecklistTemplateItems(ChecklistTemplate checklistTemplate, List<String> checklistItems) {
        List<ChecklistTemplateItem> items = checklistItems.stream()
                .map(content -> ChecklistTemplateItem.createChecklistTemplateItem(content, checklistTemplate))
                .toList();
        checklistTemplateItemRepository.saveAll(items);
    }

    private void updateTemplateStatus(ChecklistTemplate checklistTemplate, ChecklistTemplateStatus checklistTemplateStatus) {
        checklistTemplate.updateStatus(checklistTemplateStatus);
        checklistTemplateRepository.save(checklistTemplate);
    }
}
