package com.zerobase.mytable.dto.restaurant;

import com.zerobase.mytable.dto.ReservationDto;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantReservationDto extends ReservationDto {

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Min(value = 1, message = "예약 가능 인원은 1명 이상부터 입니다.")
    private Integer numberOfPeople;

}
