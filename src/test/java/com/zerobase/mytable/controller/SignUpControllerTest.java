package com.zerobase.mytable.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.mytable.dto.SignUpDto;
import com.zerobase.mytable.service.SignUpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class SignUpControllerTest {

    @MockBean
    private SignUpService signUpService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // SingUpDto.Request에 선언된 필드들 유효성 검사 성공 테스트
    @Test
    void isValidField() throws Exception {
        //given
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("asdfj@adfjsdfj.com")
                .name("asd")
                .password("dkdad154!@")
                .phone("010-1234-1234")
                .birth(LocalDate.of(1987, 2, 1))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        //when
        //then
        mockMvc.perform(post("/sign-up/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(print());

    }

    // SingUpDto.Request에 선언된 필드들 유효성 검사 실패 테스트
    @Test
    void isNotValidField() throws Exception {
        //given
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("asdfj@adfjsdfj")
                .name("asd!@")
                .password("dkdadfkslj")
                .phone("010-63-15")
                .birth(LocalDate.of(2024, 2, 1))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        //when
        //then
        mockMvc.perform(post("/sign-up/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    // 파트너 회원가입 성공 테스트
    @Test
    void successPartnerSignUp() throws Exception {
        //given
        SignUpDto.Response response = new SignUpDto.Response();
        response.setSuccessResult();

        given(signUpService.partnerSignUp(any(SignUpDto.Request.class)))
                .willReturn(response);

        //when
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("asdfj@adfjsdfj.com")
                .name("asd")
                .password("dkdad154!@")
                .phone("010-1234-1234")
                .birth(LocalDate.of(1987, 2, 1))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        //then
        mockMvc.perform(post("/sign-up/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("성공"));

    }

    // 고객 회원가입 성공 테스트
    @Test
    void successCustomerSignUp() throws Exception {
        //given
        SignUpDto.Response response = new SignUpDto.Response();
        response.setSuccessResult();

        given(signUpService.customerSignUp(any(SignUpDto.Request.class)))
                .willReturn(response);

        //when
        SignUpDto.Request request = SignUpDto.Request.builder()
                .email("asdfj@adfjsdfj.com")
                .name("asd")
                .password("dkdad154!@")
                .phone("010-1234-1234")
                .birth(LocalDate.of(1987, 2, 1))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        //then
        mockMvc.perform(post("/sign-up/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("성공"));

    }

}