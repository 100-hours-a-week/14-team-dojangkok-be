package com.dojangkok.backend.client;

import com.dojangkok.backend.dto.checklist.ChecklistGenerateRequestDto;
import com.dojangkok.backend.dto.easycontract.EasyContractGenerateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestClient restClient;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public void requestChecklistGeneration(ChecklistGenerateRequestDto request, Long templateId) {
        log.info("Requesting checklist generation to AI service for templateId: {}", templateId);

        try {
            restClient.post()
                    .uri(aiServiceUrl + "/api/checklists/{id}", templateId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Checklist generation request sent successfully for templateId: {}", templateId);
        } catch (Exception e) {
            log.error("Failed to send checklist generation request for templateId: {}", templateId, e);
            throw e;
        }
    }

    public String requestEasyContractGeneration(EasyContractGenerateRequestDto easyContractGenerateRequestDto, Long easyContractId) {
        log.info("Requesting easy contract generation to AI service for easyContractId: {}", easyContractId);

        try {
            String content = restClient.post()
                    .uri(aiServiceUrl + "/api/easycontract/{id}", easyContractId)
                    .body(easyContractGenerateRequestDto)
                    .retrieve()
                    .body(String.class);

            log.info("Easy contract generation completed for memberId: {}", easyContractId);
            return content;
        } catch (Exception e) {
            log.error("Failed to generate easy contract for memberId: {}", easyContractId, e);
            throw e;
        }
    }

}