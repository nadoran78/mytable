package com.zerobase.mytable.controller;

import com.zerobase.mytable.dto.ReservationDto;
import com.zerobase.mytable.service.ReservationService;
import com.zerobase.mytable.type.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer/reservation")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final ReservationService reservationService;

    // 예약 요청
    @PostMapping("/request")
    public CommonResponse makeReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @Valid @RequestBody ReservationDto request) {
        return reservationService.makeReservation(token, request);
    }

    // 고객이 예약한 목록 조회
    @GetMapping("/my-list")
    public Page<ReservationDto> getMyReservations(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam Integer page, @RequestParam Integer size) {
        return reservationService.customerGetMyReservations(token,
                PageRequest.of(page, size));
    }

    // 예약 상세 정보 조회
    @GetMapping("/detail")
    public ReservationDto getReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String reservationUid) {
        return reservationService.customerGetReservation(token, reservationUid);
    }

    // 예약 정보 수정
    @PutMapping("/detail/update")
    public ReservationDto updateReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String reservationUid,
            @Valid @RequestBody ReservationDto request) {
        return reservationService.updateReservation(token, reservationUid, request);
    }

    // 예약 취소
    @PostMapping("/detail/cancel")
    public ReservationDto cancelReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String reservationUid) {
        return reservationService.cancelReservation(token, reservationUid);
    }
}
