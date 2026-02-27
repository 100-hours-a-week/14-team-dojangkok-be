package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.common.util.CorrelationIdGenerator;
import com.dojangkok.backend.domain.ChecklistTemplate;
import com.dojangkok.backend.domain.ChecklistTemplateItem;
import com.dojangkok.backend.domain.LifestyleVersion;
import com.dojangkok.backend.domain.enums.ChecklistStatus;
import com.dojangkok.backend.dto.checklist.ChecklistGenerateRequestDto;
import com.dojangkok.backend.mapper.ChecklistTemplateMapper;
import com.dojangkok.backend.mq.ChecklistMqProducer;
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
    private final ChecklistMqProducer checklistMqProducer;

    @Transactional
    public void requestChecklistGeneration(Long memberId, Long lifestyleVersionId, List<String> lifestyleItems) {
        LifestyleVersion lifestyleVersion = lifestyleVersionRepository.findById(lifestyleVersionId)
                .orElseThrow(() -> new GeneralException(Code.LIFESTYLE_VERSION_NOT_FOUND));

        // 1) ChecklistTemplate 생성 (PROCESSING)
        ChecklistTemplate checklistTemplate = ChecklistTemplate.createChecklistTemplate(lifestyleVersion);
        checklistTemplateRepository.save(checklistTemplate);

        // 2) correlationId 생성 + MQ publish
        String correlationId = CorrelationIdGenerator.generate();
        ChecklistGenerateRequestDto generateRequest =
                checklistTemplateMapper.toChecklistGenerateRequestDto(lifestyleItems);

        ChecklistMqRequestDto request = ChecklistMqRequestDto.builder()
                .correlationId(correlationId)
                .templateId(checklistTemplate.getId())
                .memberId(memberId)
                .keywords(generateRequest.getKeywords())
                .build();

        checklistMqProducer.sendRequest(request);

        log.info("Checklist generation requested: templateId={}, correlationId={}",
                checklistTemplate.getId(), correlationId);
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

    @Transactional
    public void handleGenerationFailure(Long lifestyleVersionId) {
        checklistTemplateRepository.findByLifestyleVersionId(lifestyleVersionId)
                .ifPresent(template -> {
                    template.updateStatus(ChecklistStatus.FAILED);
                    checklistTemplateRepository.save(template);
                });
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
}
