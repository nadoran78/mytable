package com.zerobase.mytable.service;

import com.zerobase.mytable.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return customerRepository.getByUid(username);
    }

    // email 존재 여부 확인
    public boolean emailIsExist(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }
}
