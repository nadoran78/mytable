package com.zerobase.mytable.dto;

import com.zerobase.mytable.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomErrorResponse {
    private ErrorCode errorCode;
    private String errorMessage;
}
