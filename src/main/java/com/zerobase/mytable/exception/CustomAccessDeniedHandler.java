package com.zerobase.mytable.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.mytable.dto.CustomErrorResponse;
import com.zerobase.mytable.type.ErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 액세스 권한이 없는 리소스에 접근할 경우(AccessDeniedException 발생할 경우) 처리
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(400);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        CustomErrorResponse errorResponse = CustomErrorResponse.builder().errorCode(ErrorCode.ACCESS_DENIED)
                .errorMessage(ErrorCode.ACCESS_DENIED.getDescription())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String error = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(error);
    }
}
