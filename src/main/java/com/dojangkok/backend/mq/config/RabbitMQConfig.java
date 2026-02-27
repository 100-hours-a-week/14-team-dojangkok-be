package com.dojangkok.backend.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EASY_CONTRACT_REQUEST_QUEUE = "easy-contract.request";
    public static final String CHECKLIST_REQUEST_QUEUE = "checklist.request";
    public static final String CANCEL_REQUEST_QUEUE = "cancel.request";
    public static final String AI_RESPONSE_QUEUE = "ai.response";

    public static final String EASY_CONTRACT_REQUEST_DLQ = "easy-contract.request.dlq";
    public static final String CHECKLIST_REQUEST_DLQ = "checklist.request.dlq";
    public static final String CANCEL_REQUEST_DLQ = "cancel.request.dlq";
    public static final String AI_RESPONSE_DLQ = "ai.response.dlq";

    public static final String WAS_EXCHANGE = "was.exchange";
    public static final String FAST_EXCHANGE = "fast.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    @Bean
    public Queue easyContractRequestQueue() {
        return QueueBuilder.durable(EASY_CONTRACT_REQUEST_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("easy-contract.request")
                .ttl(180000) // 3분
                .build();
    }

    @Bean
    public Queue checklistRequestQueue(){
        return QueueBuilder.durable(CHECKLIST_REQUEST_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("checklist.request")
                .ttl(180000)
                .build();
    }

    @Bean
    public Queue cancelRequestQueue(){
        return QueueBuilder.durable(CANCEL_REQUEST_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("cancel.request")
                .ttl(180000)
                .build();
    }

    @Bean
    public Queue aiResponseQueue() {
        return QueueBuilder.durable(AI_RESPONSE_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("ai.response")
                .ttl(180000)
                .build();
    }

    @Bean
    public Queue easyContractRequestDlq() {
        return QueueBuilder.durable(EASY_CONTRACT_REQUEST_DLQ).build();
    }

    @Bean
    public Queue checklistRequestDlq() {
        return QueueBuilder.durable(CHECKLIST_REQUEST_DLQ).build();
    }

    @Bean
    public Queue cancelRequestDlq() {
        return QueueBuilder.durable(CANCEL_REQUEST_DLQ).build();
    }

    @Bean
    public Queue aiResponseDlq() {
        return QueueBuilder.durable(AI_RESPONSE_DLQ).build();
    }

    @Bean
    public DirectExchange wasExchange() {
        return new DirectExchange(WAS_EXCHANGE);
    }

    @Bean
    public DirectExchange fastExchange() {
        return new DirectExchange(FAST_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding easyContractRequestBinding(Queue easyContractRequestQueue, DirectExchange wasExchange) {
        return BindingBuilder.bind(easyContractRequestQueue)
                .to(wasExchange).with("easy-contract.request");
    }

    @Bean
    public Binding checklistRequestBinding(Queue checklistRequestQueue, DirectExchange wasExchange) {
        return BindingBuilder.bind(checklistRequestQueue)
                .to(wasExchange).with("checklist.request");
    }

    @Bean
    public Binding cancelRequestBinding(Queue cancelRequestQueue, DirectExchange wasExchange) {
        return BindingBuilder.bind(cancelRequestQueue)
                .to(wasExchange).with("cancel.request");
    }

    @Bean
    public Binding aiResponseBinding(Queue aiResponseQueue, DirectExchange fastExchange) {
        return BindingBuilder.bind(aiResponseQueue)
                .to(fastExchange).with("ai.response");
    }

    @Bean
    public Binding easyContractRequestDlqBinding(Queue easyContractRequestDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(easyContractRequestDlq)
                .to(dlxExchange).with("easy-contract.request");
    }

    @Bean
    public Binding checklistRequestDlqBinding(Queue checklistRequestDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(checklistRequestDlq)
                .to(dlxExchange).with("checklist.request");
    }

    @Bean
    public Binding cancelRequestDlqBinding(Queue cancelRequestDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(cancelRequestDlq)
                .to(dlxExchange).with("cancel.request");
    }

    @Bean
    public Binding aiResponseDlqBinding(Queue aiResponseDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(aiResponseDlq)
                .to(dlxExchange).with("ai.response");
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}