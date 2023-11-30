package com.zerobase.mytable.domain;

import com.zerobase.mytable.dto.ReservationDto;
import com.zerobase.mytable.type.ReservationStatus;
import lombok.*;
import org.hibernate.envers.AuditOverride;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AuditOverride(forClass = BaseEntity.class)
public class Reservation extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String uid;

    // 예약 요청 고객
    @ManyToOne
    private Customer customer;

    // 예약 날짜
    private LocalDateTime dateTime;

    // 예약자 이름
    private String underName;

    // 연락처
    private String phone;

    // 요청 사항
    private String specialInstruction;

    // 예약 확정 여부
    private ReservationStatus status;

    // 예약 점포
    @ManyToOne
    private Store store;

    public static Reservation from(ReservationDto request, Customer customer, Store store) {
        return Reservation.builder()
                .uid(UUID.randomUUID().toString().replace("-", ""))
                .customer(customer)
                .dateTime(LocalDateTime.of(request.getDate(), request.getTime()))
                .underName(request.getUnderName())
                .phone(request.getPhone())
                .specialInstruction(request.getSpecialInstruction())
                .status(ReservationStatus.WAITING)
                .store(store)
                .build();
    }
}
