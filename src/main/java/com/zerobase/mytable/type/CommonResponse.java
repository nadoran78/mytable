package com.zerobase.mytable.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonResponse {

    SUCCESS(0, "성공"), FAIL(-1, "실패");

    private final int code;
    private final String msg;
}
