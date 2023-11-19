package com.zerobase.mytable.controller;

import com.zerobase.mytable.dto.SignInDto;
import com.zerobase.mytable.service.SignInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sign-in")
public class SignInController {

    private final SignInService signInService;

    // 파트너 로그인
    @PostMapping("/partner")
    public SignInDto.Response partnerSignIn(@Valid @RequestBody SignInDto.Request request){
        return signInService.partnerSignIn(request);
    }

    // 고객 로그인
    @PostMapping("/customer")
    public SignInDto.Response customerSignIn(@Valid @RequestBody SignInDto.Request request){
        return signInService.customerSignIn(request);
    }
}
