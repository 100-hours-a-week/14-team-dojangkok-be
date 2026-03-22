package com.dojangkok.backend.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String EASY_CONTRACT_REQUEST_QUEUE = "quorum.easy-contract.request";
    public static final String CHECKLIST_REQUEST_QUEUE = "quorum.checklist.request";
    public static final String CANCEL_REQUEST_QUEUE = "quorum.cancel.request";
    public static final String AI_RESPONSE_QUEUE = "quorum.ai.response";
    public static final String NOTIFICATION_QUEUE = "quorum.notification.queue";

    public static final String EASY_CONTRACT_REQUEST_DLQ = "quorum.easy-contract.request.dlq";
    public static final String CHECKLIST_REQUEST_DLQ = "quorum.checklist.request.dlq";
    public static final String CANCEL_REQUEST_DLQ = "quorum.cancel.request.dlq";
    public static final String AI_RESPONSE_DLQ = "quorum.ai.response.dlq";
    public static final String NOTIFICATION_DLQ = "quorum.notification.queue.dlq";

    public static final String WAS_EXCHANGE = "was.exchange";
    public static final String FAST_EXCHANGE = "fast.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.events";

    @Bean
    public Queue easyContractRequestQueue() {
        return QueueBuilder.durable(EASY_CONTRACT_REQUEST_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("quorum.easy-contract.request")
                .ttl(300000) // 5분
                .quorum()
                .build();
    }

    @Bean
    public Queue checklistRequestQueue(){
        return QueueBuilder.durable(CHECKLIST_REQUEST_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("quorum.checklist.request")
                .ttl(300000)
                .quorum()
                .build();
    }

    @Bean
    public Queue cancelRequestQueue(){
        return QueueBuilder.durable(CANCEL_REQUEST_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("quorum.cancel.request")
                .ttl(300000)
                .quorum()
                .build();
    }

    @Bean
    public Queue aiResponseQueue() {
        return QueueBuilder.durable(AI_RESPONSE_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("quorum.ai.response")
                .ttl(300000)
                .quorum()
                .build();
    }

    @Bean
    public Queue easyContractRequestDlq() {
        return QueueBuilder.durable(EASY_CONTRACT_REQUEST_DLQ).quorum().build();
    }

    @Bean
    public Queue checklistRequestDlq() {
        return QueueBuilder.durable(CHECKLIST_REQUEST_DLQ).quorum().build();
    }

    @Bean
    public Queue cancelRequestDlq() {
        return QueueBuilder.durable(CANCEL_REQUEST_DLQ).quorum().build();
    }

    @Bean
    public Queue aiResponseDlq() {
        return QueueBuilder.durable(AI_RESPONSE_DLQ).quorum().build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey("notification.queue")
                .ttl(300000)
                .quorum()
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).quorum().build();
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
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding easyContractRequestBinding(Queue easyContractRequestQueue, DirectExchange wasExchange) {
        return BindingBuilder.bind(easyContractRequestQueue)
                .to(wasExchange).with("quorum.easy-contract.request");
    }

    @Bean
    public Binding checklistRequestBinding(Queue checklistRequestQueue, DirectExchange wasExchange) {
        return BindingBuilder.bind(checklistRequestQueue)
                .to(wasExchange).with("quorum.checklist.request");
    }

    @Bean
    public Binding cancelRequestBinding(Queue cancelRequestQueue, DirectExchange wasExchange) {
        return BindingBuilder.bind(cancelRequestQueue)
                .to(wasExchange).with("quorum.cancel.request");
    }

    @Bean
    public Binding aiResponseBinding(Queue aiResponseQueue, DirectExchange fastExchange) {
        return BindingBuilder.bind(aiResponseQueue)
                .to(fastExchange).with("quorum.ai.response");
    }

    @Bean
    public Binding easyContractRequestDlqBinding(Queue easyContractRequestDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(easyContractRequestDlq)
                .to(dlxExchange).with("quorum.easy-contract.request");
    }

    @Bean
    public Binding checklistRequestDlqBinding(Queue checklistRequestDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(checklistRequestDlq)
                .to(dlxExchange).with("quorum.checklist.request");
    }

    @Bean
    public Binding cancelRequestDlqBinding(Queue cancelRequestDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(cancelRequestDlq)
                .to(dlxExchange).with("quorum.cancel.request");
    }

    @Bean
    public Binding aiResponseDlqBinding(Queue aiResponseDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(aiResponseDlq)
                .to(dlxExchange).with("quorum.ai.response");
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange).with("chat.notification");
    }

    @Bean
    public Binding notificationDlqBinding(Queue notificationDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(notificationDlq)
                .to(dlxExchange).with("notification.queue");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Publisher Confirm NACK: cause={}, correlationData={}",
                        cause, correlationData);
            }
        });

        template.setReturnsCallback(returned -> log.error("Mandatory return: exchange={}, routingKey={}, replyCode={}, replyText={}",
                returned.getExchange(), returned.getRoutingKey(),
                returned.getReplyCode(), returned.getReplyText()));

        return template;
    }
}