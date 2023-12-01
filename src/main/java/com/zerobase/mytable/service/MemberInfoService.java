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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberInfoService {

    private final TokenProvider tokenProvider;
    private final PartnerRepository partnerRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    // 파트너 회원 정보 조회
    public MemberInfoDto getPartnerInfo(String token) {
        Partner partner = partnerRepository.getByUid(tokenProvider.getUid(token));
        return MemberInfoDto.fromPartner(partner);
    }

    // 고객 회원 정보 조회
    public MemberInfoDto getCustomerInfo(String token) {
        Customer customer = customerRepository.getByUid(
                tokenProvider.getUid(token));
        return MemberInfoDto.fromCustomer(customer);
    }

    // 파트너 회원 정보 수정
    public MemberInfoDto updatePartnerInfo(String token, MemberInfoDto request) {
        Partner partner = partnerRepository.getByUid(tokenProvider.getUid(token));

        if (!partner.getEmail().equals(request.getEmail())) {
            if (partnerRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new CustomException(ErrorCode.ALREADY_EMAIL_EXIST);
            } else {
                partner.setEmail(request.getEmail());
            }
        }
        partner.setName(request.getName());
        partner.setPassword(passwordEncoder.encode(request.getPassword()));
        partner.setPhone(request.getPhone());
        partner.setBirth(request.getBirth());

        return MemberInfoDto.fromPartner(partnerRepository.save(partner));
    }

    // 고객 회원 정보 수정
    public MemberInfoDto updateCustomerInfo(String token, MemberInfoDto request) {
        Customer customer = customerRepository.getByUid(tokenProvider.getUid(token));

        if (!customer.getEmail().equals(request.getEmail())) {
            if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new CustomException(ErrorCode.ALREADY_EMAIL_EXIST);
            } else {
                customer.setEmail(request.getEmail());
            }
        }
        customer.setName(request.getName());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setPhone(request.getPhone());
        customer.setBirth(request.getBirth());

        return MemberInfoDto.fromCustomer(customerRepository.save(customer));
    }

    // 파트너 회원 정보 삭제(패스워드 확인 후 처리)
    public CommonResponse deletePartnerInfo(String token, String password) {
        Partner partner = partnerRepository.getByUid(tokenProvider.getUid(token));

        passwordMatch(password, partner.getPassword());

        partnerRepository.delete(partner);
        return CommonResponse.SUCCESS;
    }

    // 고객 회원 정보 삭제(패스워드 확인 후 처리)
    public CommonResponse deleteCustomerInfo(String token, String password) {
        Customer customer = customerRepository.getByUid(tokenProvider.getUid(token));

        passwordMatch(password, customer.getPassword());

        customerRepository.delete(customer);
        return CommonResponse.SUCCESS;
    }

    private void passwordMatch(String requestPassword, String savedPassword){
        if (!passwordEncoder.matches(requestPassword, savedPassword)){
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }
    }


}
