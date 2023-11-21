package com.zerobase.mytable.repository;

import com.zerobase.mytable.domain.Store;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByStorename(String storename);

    List<Store> findByStorenameContains(String keyword, Pageable pageable);
}
