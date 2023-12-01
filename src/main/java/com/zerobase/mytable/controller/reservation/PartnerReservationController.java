package com.zerobase.mytable.controller.reservation;

import com.zerobase.mytable.dto.ReservationDto;
import com.zerobase.mytable.dto.StoreDto;
import com.zerobase.mytable.service.ReservationService;
import com.zerobase.mytable.service.StoreService;
import com.zerobase.mytable.type.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/partner")
@PreAuthorize("hasRole('PARTNER')")
public class PartnerReservationController {

    private final ReservationService reservationService;
    private final StoreService storeService;

    // 파트너가 관리하고 있는 점포 목록 조회
    @GetMapping("/my-stores")
    public Page<StoreDto> getMyStores(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam Integer page, @RequestParam Integer size) {
        return storeService.getMyStores(token, PageRequest.of(page, size));
    }

    // 파트너가 관리하고 있는 점포의 예약 목록 조회
    @GetMapping("/store/reservations")
    public Page<ReservationDto> getReservationsByStore(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String storename,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam Integer page, @RequestParam Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return storeService.getReservationsByStore(token, storename,
                startDate, endDate, pageRequest);
    }

    // 예약 상세정보 조회
    @GetMapping("/reservation/detail")
    public ReservationDto partnerGetReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String reservationUid) {
        return reservationService.partnerGetReservation(token, reservationUid);
    }

    // 예약 승인
    @PostMapping("/reservation/detail/confirm")
    public ReservationDto confirmReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String reservationUid) {
        return reservationService.partnerReservationConfirm(token, reservationUid);
    }

    // 예약 거절
    @PostMapping("/reservation/detail/reject")
    public ReservationDto rejectReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String reservationUid) {
        return reservationService.partnerReservationReject(token, reservationUid);
    }

    // 키오스크에서 예약 검색
    @GetMapping("/store/reservation/search")
    public Page<ReservationDto> searchReservation(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String storename, @RequestParam String underName,
            @RequestParam String phone,
            @RequestParam Integer page, @RequestParam Integer size) {
        return storeService.searchReservation(
                token, storename, underName, phone, PageRequest.of(page, size));
    }

    // 키오스크에서 예약 도착 확인
    @PostMapping("/reservation/detail/arrival-check")
    public CommonResponse arrivalConfirm(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String reservationUid) {
        return storeService.arrivalConfirm(token, reservationUid);
    }

}
