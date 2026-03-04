package com.dojangkok.backend.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterStore {

    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(Long memberId, SseEmitter emitter) {
        emitters.put(memberId, emitter);
    }

    public SseEmitter get(Long memberId) {
        return emitters.get(memberId);
    }

    public void remove(Long memberId) {
        emitters.remove(memberId);
    }

    public void removeIfMatch(Long memberId, SseEmitter emitter) {
        emitters.remove(memberId, emitter);
    }

    public Set<Long> getAllMemberIds() {
        return emitters.keySet();
    }

    public boolean isEmpty() {
        return emitters.isEmpty();
    }
}