package com.zerobase.mytable.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.mytable.dto.SignInDto;
import com.zerobase.mytable.service.SignInService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignInController.class)
class SignInControllerTest {

    @MockBean
    private SignInService signInService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 파트너 로그인 성공
    @Test
    @WithMockUser
    void successPartnerSignIn() throws Exception {
        //given
        SignInDto.Response response = new SignInDto.Response();
        response.setSuccessResult();

        given(signInService.partnerSignIn(any(SignInDto.Request.class)))
                .willReturn(response);
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abcd@abcd.com")
                .password("abc123!@#")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);
        //then
        mockMvc.perform(post("/sign-in/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("성공"));
    }

    // 고객 로그인 성공
    @Test
    @WithMockUser
    void successCustomerSignIn() throws Exception {
        //given
        SignInDto.Response response = new SignInDto.Response();
        response.setSuccessResult();

        given(signInService.customerSignIn(any(SignInDto.Request.class)))
                .willReturn(response);
        //when
        SignInDto.Request request = SignInDto.Request.builder()
                .email("abcd@abcd.com")
                .password("abc123!@#")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);
        //then
        mockMvc.perform(post("/sign-in/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("성공"));
    }

}