package com.zerobase.mytable.controller;

import com.zerobase.mytable.dto.MemberInfoDto;
import com.zerobase.mytable.service.MemberInfoService;
import com.zerobase.mytable.type.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

    // 파트너 회원 정보 조회
    @GetMapping("/partner/info")
    @PreAuthorize("hasRole('PARTNER')")
    public MemberInfoDto getPartnerInfo(
            @RequestHeader(name = "X-AUTH-TOKEN") String token) {
        return memberInfoService.getPartnerInfo(token);
    }

    // 고객 회원 정보 조회
    @GetMapping("/customer/info")
    @PreAuthorize("hasRole('CUSTOMER')")
    public MemberInfoDto getCustomerInfo(
            @RequestHeader(name = "X-AUTH-TOKEN") String token) {
        return memberInfoService.getCustomerInfo(token);
    }

    // 파트너 회원 정보 수정
    @PutMapping("/partner/info")
    @PreAuthorize("hasRole('PARTNER')")
    public MemberInfoDto updatePartnerInfo(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @Valid @RequestBody MemberInfoDto request) {
        return memberInfoService.updatePartnerInfo(token, request);
    }

    // 고객 회원 정보 수정
    @PutMapping("/customer/info")
    @PreAuthorize("hasRole('CUSTOMER')")
    public MemberInfoDto updateCustomerInfo(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @Valid @RequestBody MemberInfoDto request) {
        return memberInfoService.updateCustomerInfo(token, request);
    }

    // 파트너 회원 정보 삭제
    @DeleteMapping("/partner/info")
    @PreAuthorize("hasRole('PARTNER')")
    public CommonResponse deletePartnerInfo(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestBody String password) {
        return memberInfoService.deletePartnerInfo(token, password);
    }

    // 고객 회원 정보 삭제
    @DeleteMapping("/customer/info")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CommonResponse deleteCustomerInfo(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestBody String password) {
        return memberInfoService.deleteCustomerInfo(token, password);
    }
}
