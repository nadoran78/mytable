package com.zerobase.mytable.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 토큰 관련
    INVALID_TOKEN("유효하지 않은 토큰입니다."),

    // 회원가입 관련
    ALREADY_EMAIL_EXIST("이미 존재하는 이메일입니다."),
    NOT_FOUND_USER("존재하지 않는 회원입니다."),
    INCORRECT_PASSWORD("패스워드가 일치하지 않습니다.");

    private final String description;
}
