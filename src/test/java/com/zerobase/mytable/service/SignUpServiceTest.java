package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Partner;
import com.zerobase.mytable.dto.SignUpDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.CustomerRepository;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PartnerService partnerService;
    @Mock
    private CustomerService customerService;
    @InjectMocks
    private SignUpService signUpService;

    // 파트너 회원가입 성공 테스트
    @Test
    void successPartnerSignUp() {
        //given
        Mockito.when(partnerService.emailIsExist(anyString()))
                .thenReturn(false);
        Mockito.when(passwordEncoder.encode(anyString()))
                .thenReturn("abcd");
        Mockito.when(partnerRepository.save(any(Partner.class)))
                .then(returnsFirstArg());
        //when
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("abcd@abcd.com")
                .name("OneEyed")
                .password("abdc123!@#")
                .phone("010-1234-1234")
                .birth(LocalDate.of(1990, 1, 1))
                .build();
        SignUpDto.Response response = signUpService.partnerSignUp(request);

        //then
        assertTrue(response.isSuccess());
        assertEquals(response.getCode(), 0);
        assertEquals(response.getMsg(), "성공");
    }

    // 파트너 회원가입 시 이미 존재하는 이메일일 경우 처리 테스트
    @Test
    void partnerSignUp_AlreadyEmailExist() {
        //given
        Mockito.when(partnerService.emailIsExist(anyString()))
                .thenReturn(true);

        //when
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("abcd@abcd.com")
                .name("OneEyed")
                .password("abdc123!@#")
                .phone("010-1234-1234")
                .birth(LocalDate.of(1990, 1, 1))
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> signUpService.partnerSignUp(request));

        //then
        assertEquals(ErrorCode.ALREADY_EMAIL_EXIST, customException.getErrorCode());
    }

    // 고객 회원가입 성공 테스트
    @Test
    void successCustomerSignUp() {
        //given
        Mockito.when(customerService.emailIsExist(anyString()))
                .thenReturn(false);
        Mockito.when(passwordEncoder.encode(anyString()))
                .thenReturn("abcd");
        Mockito.when(customerRepository.save(any(Customer.class)))
                .then(returnsFirstArg());
        //when
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("abcd@abcd.com")
                .name("OneEyed")
                .password("abdc123!@#")
                .phone("010-1234-1234")
                .birth(LocalDate.of(1990, 1, 1))
                .build();
        SignUpDto.Response response = signUpService.customerSignUp(request);

        //then
        assertTrue(response.isSuccess());
        assertEquals(response.getCode(), 0);
        assertEquals(response.getMsg(), "성공");
    }

    // 고객 회원가입 시 이미 존재하는 이메일일 경우 처리 테스트
    @Test
    void customerSignUp_AlreadyEmailExist() {
        //given
        Mockito.when(customerService.emailIsExist(anyString()))
                .thenReturn(true);

        //when
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("abcd@abcd.com")
                .name("OneEyed")
                .password("abdc123!@#")
                .phone("010-1234-1234")
                .birth(LocalDate.of(1990, 1, 1))
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> signUpService.customerSignUp(request));

        //then
        assertEquals(ErrorCode.ALREADY_EMAIL_EXIST, customException.getErrorCode());
    }

}