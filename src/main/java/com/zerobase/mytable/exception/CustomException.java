package com.zerobase.mytable.exception;

import com.zerobase.mytable.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomException extends RuntimeException {
    private ErrorCode errorCode;
    private String errorMessage;

    public CustomException(ErrorCode errorCode){
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
