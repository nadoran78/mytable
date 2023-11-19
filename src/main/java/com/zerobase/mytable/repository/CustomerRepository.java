package com.zerobase.mytable.repository;

import com.zerobase.mytable.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer getByUid(String uid);
    Optional<Customer> findByEmail(String email);
}
