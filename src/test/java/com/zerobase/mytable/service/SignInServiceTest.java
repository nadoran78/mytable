package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Partner;
import com.zerobase.mytable.dto.SignInDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.CustomerRepository;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.ErrorCode;
import com.zerobase.mytable.type.UserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class SignInServiceTest {

    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private SignInService signInService;

    // 파트너 로그인 성공 테스트
    @Test
    void successPartnerSignIn() {
        //given
        Mockito.when(partnerRepository.findByEmail(anyString()))
                .thenReturn(Optional.ofNullable(Partner.builder()
                        .uid("abc")
                        .password("123abc!@#")
                        .roles(Collections.singletonList(UserType.PARTNER.toString()))
                        .build()));
        Mockito.when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        Mockito.when(tokenProvider.createToken(anyString(), anyList()))
                .thenReturn("fighting");
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abc@abc.com")
                .password("123!@#abc")
                .build();

        SignInDto.Response response = signInService.partnerSignIn(request);
        //then
        assertTrue(response.isSuccess());
        assertEquals(response.getCode(), 0);
        assertEquals(response.getMsg(), "성공");
    }

    // 회원가입한 파트너 이메일 없을 경우 처리 테스트
    @Test
    void partnerSignIn_NotFoundUser() {
        //given
        Mockito.when(partnerRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abc@abc.com")
                .password("123!@#abc")
                .build();
        CustomException exception = assertThrows(CustomException.class,
                () -> signInService.partnerSignIn(request));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_FOUND_USER);
    }

    // 파트너 로그인 할 경우 패스워드 불일치 시 처리
    @Test
    void partnerSignIn_IncorrectPassword() {
        //given
        Mockito.when(partnerRepository.findByEmail(anyString()))
                .thenReturn(Optional.ofNullable(Partner.builder()
                        .uid("abc")
                        .password("123abc!@#")
                        .roles(Collections.singletonList(UserType.PARTNER.toString()))
                        .build()));
        Mockito.when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abc@abc.com")
                .password("123!@#abc")
                .build();
        CustomException exception = assertThrows(CustomException.class,
                () -> signInService.partnerSignIn(request));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.INCORRECT_PASSWORD);
    }

    // 고객 로그인 성공 테스트
    @Test
    void successCustomerSignIn() {
        //given
        Mockito.when(customerRepository.findByEmail(anyString()))
                .thenReturn(Optional.ofNullable(Customer.builder()
                        .uid("abc")
                        .password("123abc!@#")
                        .roles(Collections.singletonList(UserType.CUSTOMER.toString()))
                        .build()));
        Mockito.when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        Mockito.when(tokenProvider.createToken(anyString(), anyList()))
                .thenReturn("fighting");
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abc@abc.com")
                .password("123!@#abc")
                .build();

        SignInDto.Response response = signInService.customerSignIn(request);
        //then
        assertTrue(response.isSuccess());
        assertEquals(response.getCode(), 0);
        assertEquals(response.getMsg(), "성공");
    }

    // 회원가입한 고객 이메일 없을 경우 처리 테스트
    @Test
    void customerSignIn_NotFoundUser() {
        //given
        Mockito.when(customerRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abc@abc.com")
                .password("123!@#abc")
                .build();
        CustomException exception = assertThrows(CustomException.class,
                () -> signInService.customerSignIn(request));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_FOUND_USER);
    }

    // 고객 로그인 할 경우 패스워드 불일치 시 처리
    @Test
    void customerSignIn_IncorrectPassword() {
        //given
        Mockito.when(customerRepository.findByEmail(anyString()))
                .thenReturn(Optional.ofNullable(Customer.builder()
                        .uid("abc")
                        .password("123abc!@#")
                        .roles(Collections.singletonList(UserType.CUSTOMER.toString()))
                        .build()));
        Mockito.when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abc@abc.com")
                .password("123!@#abc")
                .build();
        CustomException exception = assertThrows(CustomException.class,
                () -> signInService.customerSignIn(request));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.INCORRECT_PASSWORD);
    }
}