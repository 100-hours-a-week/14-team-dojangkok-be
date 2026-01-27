package com.dojangkok.backend.auth.jwt;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자의 memberId를 주입받는 어노테이션
 * 
 * 사용 예시:
 * @GetMapping("/me")
 * public ResponseEntity<?> getMe(@CurrentMemberId Long memberId) {
 *     ...
 * }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentMemberId {
}
