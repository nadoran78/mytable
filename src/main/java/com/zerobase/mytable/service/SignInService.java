package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Partner;
import com.zerobase.mytable.dto.SignInDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.CustomerRepository;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SignInService {

    private final PartnerRepository partnerRepository;
    private final CustomerRepository customerRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 파트너 로그인(이메일 존재 여부 확인, 패스워드 확인, 로그인 결과 확인 및 반환)
    public SignInDto.Response partnerSignIn(SignInDto.Request request) {
        Partner partner = partnerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        passwordMatch(request.getPassword(), partner.getPassword());

        SignInDto.Response response = SignInDto.Response.builder()
                .token(tokenProvider.createToken(partner.getUid(), partner.getRoles()))
                .build();
        response.setSuccessResult();

        return response;
    }

    // 파트너 로그인(이메일 존재 여부 확인, 패스워드 확인, 로그인 결과 확인 및 반환)
    public SignInDto.Response customerSignIn(SignInDto.Request request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        passwordMatch(request.getPassword(), customer.getPassword());

        SignInDto.Response response = SignInDto.Response.builder()
                .token(tokenProvider.createToken(customer.getUid(), customer.getRoles()))
                .build();
        response.setSuccessResult();

        return response;
    }

    private void passwordMatch(String requestPassword, String savedPassword){
        if (!passwordEncoder.matches(requestPassword, savedPassword)){
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }
    }

}
