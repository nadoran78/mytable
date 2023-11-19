package com.zerobase.mytable.dto;

import com.zerobase.mytable.type.CommonResponse;
import lombok.*;

import javax.validation.constraints.NotNull;

public class SignInDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Request{

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String email;

        @NotNull(message = "반드시 값이 있어야 합니다.")
        private String password;

    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private boolean success;
        private int code;
        private String msg;
        private String token;

        public void setSuccessResult(){
            this.success = true;
            this.code = CommonResponse.SUCCESS.getCode();
            this.msg = CommonResponse.SUCCESS.getMsg();
        }

        public void setFailResult() {
            this.success = false;
            this.code = CommonResponse.SUCCESS.getCode();
            this.msg = CommonResponse.SUCCESS.getMsg();
        }
    }
}
