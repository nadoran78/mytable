package com.zerobase.mytable.repository;

import com.zerobase.mytable.domain.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Partner getByUid(String uid);
    Optional<Partner> findByEmail(String email);
}
