package com.zerobase.mytable.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_REQUEST("잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),

    // 토큰 관련
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    ACCESS_DENIED("접근 권한이 없습니다."),

    // 회원가입 관련
    ALREADY_EMAIL_EXIST("이미 존재하는 이메일입니다."),
    NOT_FOUND_USER("존재하지 않는 회원입니다."),
    INCORRECT_PASSWORD("패스워드가 일치하지 않습니다."),

    // 점포 등록 관련
    ALREADY_REGISTERED_STORENAME("이미 존재하는 점포명입니다."),
    NOT_FOUND_STORE("존재하지 않는 점포입니다."),

    // 예약 관련
    RESERVATION_DATE_MUST_BE_IN_A_MONTH("예약은 한달 이내만 가능합니다."),
    TOO_MANY_NUMBER_OF_PEOPLE("예약인원을 수용할 테이블이 없습니다. 점포로 문의해주세요."),
    RESERVATION_NOT_FOUND("해당 예약을 찾을 수 없습니다."),
    CANNOT_UPDATE_STORE("점포를 수정할 수 없습니다. 해당 점포로 새로운 예약을 진행해 주세요."),
    ACCESS_ONLY_REQUESTED_CUSTOMER("예약정보 조회는 예약을 요청한 고객만 가능합니다."),
    ACCESS_ONLY_STORE_OWNER("해당 점포 점주만 가능한 기능입니다."),
    NOT_FOUND_RESERVATION("예약 정보가 없습니다. 예약자명과 전화번호를 확인해주세요."),
    NOT_RESERVATION_STORE("예약한 매장이 아닙니다."),
    ENTRANCE_NOT_ON_TIME("도착 확인은 예약 시간 10분 전부터 가능합니다."),
    TIME_OVER("입장 가능 시간이 지났습니다."),

    // 리뷰 관련
    DID_NOT_USE_THIS_STORE("리뷰는 해당 점포를 사용한 후에 작성하여 주세요."),
    NOT_FOUND_REVIEW("존재하지 않는 리뷰입니다."),
    ONLY_WORKS_WITH_WRITER("리뷰 작성자만 수정, 삭제 가능합니다."),
    CANNOT_UPDATE_STORENAME("점포를 수정할 수는 없습니다.");


    private final String description;
}
