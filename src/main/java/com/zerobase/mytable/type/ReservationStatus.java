package com.zerobase.mytable.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    WAITING("확정 대기"),
    CONFIRM("확정"),
    CANCEL("취소"),
    DENIED("거절"),
    ARRIVED("실행"),
    NO_SHOW("미실행");

    private final String msg;
}
