package com.zerobase.mytable.service;

import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.type.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartnerService implements UserDetailsService {

    private final PartnerRepository partnerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return partnerRepository.getByUid(username) ;
    }

    // email 존재 여부 확인
    public boolean emailIsExist(String email) {
        return partnerRepository.findByEmail(email).isPresent();
    }
}
