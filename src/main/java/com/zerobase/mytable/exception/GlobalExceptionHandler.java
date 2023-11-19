package com.zerobase.mytable.exception;

import com.zerobase.mytable.dto.CustomErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException is occurred.", e);

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors()
                .forEach(error -> errors.put(((FieldError) error).getField(),
                        error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(CustomException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public CustomErrorResponse handleCustomException(CustomException e) {
        log.error("{} is occurred.", e.getErrorCode());
        return new CustomErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }

}
