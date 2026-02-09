package com.dojangkok.backend.facade;

import com.dojangkok.backend.client.AiServiceClient;
import com.dojangkok.backend.service.ChecklistTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.List;

/**
 * 체크리스트 템플릿 생성 프로세스를 조율하는 Facade
 * 복잡한 비동기 체크리스트 생성 흐름을 단순화된 인터페이스로 제공
 * 1. 템플릿 초기 생성 (PROCESSING 상태)
 * 2. AI 서비스 호출
 * 3. 실패 시 상태 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChecklistTemplateFacade {

    private final ChecklistTemplateService checklistTemplateService;
    private final AiServiceClient aiServiceClient;

    /**
     * 체크리스트 템플릿 생성
     * */
    public void initiateChecklistGeneration(Long lifestyleVersionId, List<String> lifestyleItems) {
        try {
            aiServiceClient.requestChecklistGeneration(
                    checklistTemplateService.generateChecklist(lifestyleVersionId, lifestyleItems));
        } catch (Exception e) {
            log.error("Failed to initiate checklist generation for lifestyleVersionId: {}", lifestyleVersionId, e);
            checklistTemplateService.handleGenerationFailure(lifestyleVersionId);
            throw e; // listener가 에러를 인지할 수 있게 하기 위함
        }
    }

}
