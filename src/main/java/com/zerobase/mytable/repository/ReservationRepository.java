package com.zerobase.mytable.repository;

import com.zerobase.mytable.domain.Reservation;
import com.zerobase.mytable.domain.Store;
import com.zerobase.mytable.type.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByUid(String uid);
    List<Reservation> findAllByUnderNameAndPhoneAndStoreAndStatus(
            String underName, String phone, Store store, ReservationStatus status);
    List<Reservation> findAllByStatusAndDateTimeBefore(
            ReservationStatus reservationStatus, LocalDateTime dateTime);
}
