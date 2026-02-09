package com.dojangkok.backend.client;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.dto.checklist.ChecklistTemplateGenerateContext;
import com.dojangkok.backend.dto.easycontract.EasyContractGenerateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestClient restClient;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public void requestChecklistGeneration(ChecklistTemplateGenerateContext checklistTemplateGenerateContext) {
        Long templateId = checklistTemplateGenerateContext.templateId();
        log.info("Requesting checklist generation to AI service for templateId: {}", templateId);

        try {
            restClient.post()
                    .uri(aiServiceUrl + "/api/checklists/{id}", templateId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(checklistTemplateGenerateContext.checklistGenerateRequestDto())
                    .retrieve()
                    .toBodilessEntity();

            log.info("Checklist generation request sent successfully for templateId: {}", templateId);
        } catch (HttpServerErrorException e) {
            log.error("AI Service server error for templateId: {}, response: {}", templateId, e.getResponseBodyAsString(), e);
            throw new GeneralException(Code.AI_SERVICE_ERROR);
        } catch (HttpClientErrorException e) {
            log.error("AI Service client error for templateId: {}, response: {}", templateId, e.getResponseBodyAsString(), e);
            throw new GeneralException(Code.AI_SERVICE_ERROR);
        } catch (ResourceAccessException e) {
            log.error("AI Service connection failed for templateId: {}", templateId, e);
            throw new GeneralException(Code.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Failed to send checklist generation request for templateId: {}", templateId, e);
            throw new GeneralException(Code.AI_SERVICE_UNAVAILABLE);
        }
    }

    public String requestEasyContractGeneration(EasyContractGenerateRequestDto easyContractGenerateRequestDto, Long easyContractId) {
        log.info("Requesting easy contract generation to AI service for easyContractId: {}", easyContractId);

        try {
            String content = restClient.post()
                    .uri(aiServiceUrl + "/api/easycontract/{id}", easyContractId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_MARKDOWN, MediaType.TEXT_PLAIN, MediaType.ALL)  // 마크다운 우선 수락
                    .body(easyContractGenerateRequestDto)
                    .retrieve()
                    .body(String.class);

            log.info("Easy contract generation completed for easyContractId: {}", easyContractId);
            return content;
        } catch (HttpServerErrorException e) {
            log.error("AI Service server error for easyContractId: {}, response: {}", easyContractId, e.getResponseBodyAsString(), e);
            throw new GeneralException(Code.AI_SERVICE_ERROR);
        } catch (HttpClientErrorException e) {
            log.error("AI Service client error for easyContractId: {}, response: {}", easyContractId, e.getResponseBodyAsString(), e);
            throw new GeneralException(Code.AI_SERVICE_ERROR);
        } catch (ResourceAccessException e) {
            log.error("AI Service connection failed for easyContractId: {}", easyContractId, e);
            throw new GeneralException(Code.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Failed to generate easy contract for easyContractId: {}", easyContractId, e);
            throw new GeneralException(Code.AI_SERVICE_UNAVAILABLE);
        }
    }

}