package com.zerobase.mytable.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class StoreRegisterDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Request{

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String storename;

        @Pattern(regexp = "^0([0-9]{1,3}?)-?([0-9]{3,4})-?([0-9]{4})$",
                message = "전화번호가 유효하지 않습니다.")
        private String phone;

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String sido;

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String sigungu;

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String roadname;

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String detailAddress;

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String description;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String msg;
    }
}
