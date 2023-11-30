package com.zerobase.mytable.domain.restaurant;

import com.zerobase.mytable.domain.Reservation;
import com.zerobase.mytable.type.Table;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
public class RestaurantReservation extends Reservation {

    // 예약 인원
    private Integer numberOfPeople;

    @Embedded
    private Table table;
}
