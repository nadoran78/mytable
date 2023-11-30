package com.zerobase.mytable.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.mytable.domain.Reservation;
import com.zerobase.mytable.type.ReservationStatus;
import lombok.*;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationDto {

    @Null(message = "null이어야 합니다.")
    private String uid;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @FutureOrPresent(message = "예약일은 오늘 이후여야 합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd",
            timezone = "Asia/Seoul")
    private LocalDate date;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime time;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    private String underName;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Pattern(regexp = "^01([0|1|6|7|8|9]?)-?([0-9]{3,4})-?([0-9]{4})$")
    private String phone;

    private String specialInstruction;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    private String storename;

    @Null(message = "null이어야 합니다.")
    private ReservationStatus status;

    public static ReservationDto from(Reservation reservation){
        return ReservationDto.builder()
                .uid(reservation.getUid())
                .date(reservation.getDateTime().toLocalDate())
                .time(reservation.getDateTime().toLocalTime())
                .underName(reservation.getUnderName())
                .phone(reservation.getPhone())
                .specialInstruction(reservation.getSpecialInstruction())
                .storename(reservation.getStore().getStorename())
                .status(reservation.getStatus())
                .build();
    }

}
