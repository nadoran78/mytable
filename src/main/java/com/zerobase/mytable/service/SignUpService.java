package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Partner;
import com.zerobase.mytable.dto.SignUpDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.CustomerRepository;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SignUpService {

    private final PartnerRepository partnerRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final PartnerService partnerService;
    private final CustomerService customerService;

    // 파트너 회원가입(이메일 존재 여부 확인, password 암호화해서 저장, 회원가입 결과 확인 및 반환)
    public SignUpDto.Response partnerSignUp(SignUpDto.Request request) {
        if (partnerService.emailIsExist(request.getEmail())) {
            throw new CustomException(ErrorCode.ALREADY_EMAIL_EXIST);
        } else {
            request.setPassword(passwordEncoder.encode(request.getPassword()));
            Partner savedPartner = partnerRepository.save(Partner.from(request));

            SignUpDto.Response response = new SignUpDto.Response();

            if (!savedPartner.getName().isEmpty()) {
                response.setSuccessResult();
            } else {
                response.setFailResult();
            }

            return response;
        }
    }

    // 고객 회원가입(이메일 존재 여부 확인, password 암호화해서 저장, 회원가입 결과 확인 및 반환)
    public SignUpDto.Response customerSignUp(SignUpDto.Request request) {
        if (customerService.emailIsExist(request.getEmail())) {
            throw new CustomException(ErrorCode.ALREADY_EMAIL_EXIST);
        } else {
            request.setPassword(passwordEncoder.encode(request.getPassword()));
            Customer savedCustomer = customerRepository.save(Customer.from(request));

            SignUpDto.Response response = new SignUpDto.Response();

            if (!savedCustomer.getName().isEmpty()) {
                response.setSuccessResult();
            } else {
                response.setFailResult();
            }

            return response;
        }
    }

}
