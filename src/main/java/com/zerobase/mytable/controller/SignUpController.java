package com.zerobase.mytable.controller;

import com.zerobase.mytable.dto.SignUpDto;
import com.zerobase.mytable.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sign-up")
public class SignUpController {

    private final SignUpService signUpService;

    // 파트너 회원가입
    @PostMapping("/partner")
    public SignUpDto.Response partnerSignUp(@Valid @RequestBody SignUpDto.Request request){
        return signUpService.partnerSignUp(request);
    }

    // 고객 회원가입
    @PostMapping("/customer")
    public SignUpDto.Response customerSignUp(@Valid @RequestBody SignUpDto.Request request){
        return signUpService.customerSignUp(request);
    }

}
