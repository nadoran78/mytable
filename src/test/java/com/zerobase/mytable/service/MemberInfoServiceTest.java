package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Partner;
import com.zerobase.mytable.dto.MemberInfoDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.CustomerRepository;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.CommonResponse;
import com.zerobase.mytable.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberInfoServiceTest {

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private MemberInfoService memberInfoService;

    // 파트너 회원 정보 조회 성공 테스트
    @Test
    void successGetPartnerInfo() {
        //given
        Partner partner = Partner.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(partnerRepository.getByUid(anyString()))
                .willReturn(partner);
        //when
        MemberInfoDto memberInfoDto =
                memberInfoService.getPartnerInfo("abc");
        //then
        assertEquals(memberInfoDto.getEmail(), "abc");
        assertEquals(memberInfoDto.getName(), "abc");
        assertEquals(memberInfoDto.getPassword(), "**********");
        assertEquals(memberInfoDto.getPhone(), "010-1111-1111");
        assertEquals(memberInfoDto.getBirth(), LocalDate.of(1900, 1, 1));
    }

    // 고객 회원 정보 조회 성공 테스트
    @Test
    void successGetCustomerInfo() {
        //given
        Customer customer = Customer.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        //when
        MemberInfoDto memberInfoDto =
                memberInfoService.getCustomerInfo("abc");
        //then
        assertEquals(memberInfoDto.getEmail(), "abc");
        assertEquals(memberInfoDto.getName(), "abc");
        assertEquals(memberInfoDto.getPassword(), "**********");
        assertEquals(memberInfoDto.getPhone(), "010-1111-1111");
        assertEquals(memberInfoDto.getBirth(), LocalDate.of(1900, 1, 1));
    }

    // 파트너 회원 정보 수정 성공 테스트
    @Test
    void successUpdatePartnerInfo() {
        //given
        Partner partner = Partner.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(partnerRepository.getByUid(anyString()))
                .willReturn(partner);
        given(partnerRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString()))
                .willReturn("abc");
        given(partnerRepository.save(any(Partner.class)))
                .will(returnsFirstArg());
        //when
        MemberInfoDto request = MemberInfoDto.builder()
                .email("aaa")
                .name("bbb")
                .password("1")
                .phone("123")
                .birth(LocalDate.of(2000, 2, 2))
                .build();
        MemberInfoDto memberInfoDto =
                memberInfoService.updatePartnerInfo("abc", request);
        //then
        assertEquals(memberInfoDto.getEmail(), "aaa");
        assertEquals(memberInfoDto.getName(), "bbb");
        assertEquals(memberInfoDto.getPassword(), "**********");
        assertEquals(memberInfoDto.getPhone(), "123");
        assertEquals(memberInfoDto.getBirth(), LocalDate.of(2000, 2, 2));
    }

    // 파트너 회원 정보 시 수정하려는 이메일이 이미 존재할 경우 예외처리
    @Test
    void updatePartnerInfo_AlreadyEmailExist() {
        //given
        Partner partner = Partner.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(partnerRepository.getByUid(anyString()))
                .willReturn(partner);
        given(partnerRepository.findByEmail(anyString()))
                .willReturn(Optional.of(partner));
        //when
        MemberInfoDto request = MemberInfoDto.builder()
                .email("aaa")
                .name("bbb")
                .password("1")
                .phone("123")
                .birth(LocalDate.of(2000, 2, 2))
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> memberInfoService.updatePartnerInfo("abc", request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ALREADY_EMAIL_EXIST);
    }

    // 파트너 회원 정보 수정 성공 테스트
    @Test
    void successUpdateCustomerInfo() {
        //given
        Customer customer = Customer.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        given(customerRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString()))
                .willReturn("abc");
        given(customerRepository.save(any(Customer.class)))
                .will(returnsFirstArg());
        //when
        MemberInfoDto request = MemberInfoDto.builder()
                .email("aaa")
                .name("bbb")
                .password("1")
                .phone("123")
                .birth(LocalDate.of(2000, 2, 2))
                .build();
        MemberInfoDto memberInfoDto =
                memberInfoService.updateCustomerInfo("abc", request);
        //then
        assertEquals(memberInfoDto.getEmail(), "aaa");
        assertEquals(memberInfoDto.getName(), "bbb");
        assertEquals(memberInfoDto.getPassword(), "**********");
        assertEquals(memberInfoDto.getPhone(), "123");
        assertEquals(memberInfoDto.getBirth(), LocalDate.of(2000, 2, 2));
    }

    // 파트너 회원 정보 시 수정하려는 이메일이 이미 존재할 경우 예외처리
    @Test
    void updateCustomerInfo_AlreadyEmailExist() {
        //given
        Customer customer = Customer.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        given(customerRepository.findByEmail(anyString()))
                .willReturn(Optional.of(customer));
        //when
        MemberInfoDto request = MemberInfoDto.builder()
                .email("aaa")
                .name("bbb")
                .password("1")
                .phone("123")
                .birth(LocalDate.of(2000, 2, 2))
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> memberInfoService.updateCustomerInfo("abc", request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ALREADY_EMAIL_EXIST);
    }

    // 파트너 회원 정보 삭제 성공 테스트
    @Test
    void successDeletePartnerInfo() {
        //given
        Partner partner = Partner.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(partnerRepository.getByUid(anyString()))
                .willReturn(partner);
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        //when
        CommonResponse commonResponse =
                memberInfoService.deletePartnerInfo("abc", "abc");
        //then
        assertEquals(commonResponse, CommonResponse.SUCCESS);
    }

    // 파트너 회원 정보 삭제 시 패스워드 불일치하면 예외 처리
    @Test
    void deletePartnerInfo_IncorrectPassword() {
        //given
        Partner partner = Partner.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(partnerRepository.getByUid(anyString()))
                .willReturn(partner);
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> memberInfoService.deletePartnerInfo("abc", "abc"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.INCORRECT_PASSWORD);
    }

    // 파트너 회원 정보 삭제 성공 테스트
    @Test
    void successDeleteCustomerInfo() {
        //given
        Customer customer = Customer.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        //when
        CommonResponse commonResponse =
                memberInfoService.deleteCustomerInfo("abc", "abc");
        //then
        assertEquals(commonResponse, CommonResponse.SUCCESS);
    }

    // 파트너 회원 정보 삭제 시 패스워드 불일치하면 예외 처리
    @Test
    void deleteCustomerInfo_IncorrectPassword() {
        //given
        Customer customer = Customer.builder()
                .email("abc")
                .name("abc")
                .password("asdf")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1900, 1, 1))
                .build();
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        given(customerRepository.getByUid(anyString()))
                .willReturn(customer);
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> memberInfoService.deleteCustomerInfo("abc", "abc"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.INCORRECT_PASSWORD);
    }
}