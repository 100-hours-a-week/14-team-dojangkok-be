package com.dojangkok.backend.controller;

import com.dojangkok.backend.auth.jwt.CurrentMemberId;
import com.dojangkok.backend.sse.SseEmitterStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/sse")
public class SseController {

    private final SseEmitterStore emitterStore;

    @GetMapping("/connection")
    public SseEmitter connect(@CurrentMemberId Long memberId) {
        // 기존 연결이 있으면 끊고 새로 생성
        SseEmitter existing = emitterStore.get(memberId);
        if (existing != null) {
            existing.complete();
            emitterStore.remove(memberId);
        }

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분

        emitterStore.save(memberId, emitter);

        emitter.onCompletion(() -> emitterStore.remove(memberId));
        emitter.onTimeout(() -> emitterStore.remove(memberId));
        emitter.onError(e -> emitterStore.remove(memberId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            // 데이터 보내려다 실패하면(짧은 새에 유저가 창을 닫음 등) 잔여 데이터가 남지 않게 저장소에서 즉시 삭제
            emitterStore.remove(memberId);
        }

        return emitter;
    }
}
