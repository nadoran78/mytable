package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Reservation;
import com.zerobase.mytable.domain.Store;
import com.zerobase.mytable.dto.ReservationDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.CustomerRepository;
import com.zerobase.mytable.repository.ReservationRepository;
import com.zerobase.mytable.repository.StoreRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.CommonResponse;
import com.zerobase.mytable.type.ErrorCode;
import com.zerobase.mytable.type.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final TokenProvider tokenProvider;
    private final SendMessageService sendMessageService;

    // 예약 요청
    @Transactional
    public CommonResponse makeReservation(String token, ReservationDto request) {
        Store store = storeRepository.findByStorename(request.getStorename())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        if (!LocalDate.now().plusMonths(1).isAfter(request.getDate())) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_MUST_BE_IN_A_MONTH);
        }

        Customer customer = customerRepository.getByUid(tokenProvider.getUid(token));
        Reservation savedReservation = reservationRepository.save(
                Reservation.from(request, customer, store));

        if (!savedReservation.getUnderName().isEmpty()) {
            sendReservationMessageToPartner(savedReservation);
            return CommonResponse.SUCCESS;
        } else {
            return CommonResponse.FAIL;
        }
    }

    // 고객이 예약한 예약 리스트 조회
    public Page<ReservationDto> customerGetMyReservations(
            String token, PageRequest pageRequest) {
        Customer customer = customerRepository.getByUid(tokenProvider.getUid(token));

        List<ReservationDto> reservationDtos =
                customer.getReservations().stream()
                        .sorted(Comparator.comparing(Reservation::getDateTime))
                        .map(ReservationDto::from)
                        .collect(Collectors.toList());

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), reservationDtos.size());

        return new PageImpl<>(
                reservationDtos.subList(start, end),
                pageRequest,
                reservationDtos.size());
    }

    // 고객 예약 상세정보 조회
    public ReservationDto customerGetReservation(String token, String reservationUid) {

        Reservation reservation = getReservationAndValidateCustomer(
                token, reservationUid);

        return ReservationDto.from(reservation);
    }

    // 고객 예약 수정
    public ReservationDto updateReservation(String token, String reservationUid,
                                            ReservationDto request) {
        Reservation reservation = getReservationAndValidateCustomer(
                token, reservationUid);

        if (!reservation.getStore().getStorename().equals(request.getStorename())) {
            throw new CustomException(ErrorCode.CANNOT_UPDATE_STORE);
        }

        reservation.setDateTime(LocalDateTime.of(request.getDate(), request.getTime()));
        reservation.setUnderName(request.getUnderName());
        reservation.setPhone(request.getPhone());
        reservation.setSpecialInstruction(request.getSpecialInstruction());
        reservation.setStatus(ReservationStatus.WAITING);

        Reservation savedReservation = reservationRepository.save(reservation);
        sendReservationMessageToPartner(savedReservation);

        return ReservationDto.from(savedReservation);
    }

    // 고객 예약 취소
    public ReservationDto cancelReservation(String token, String reservationUid) {
        Reservation reservation = getReservationAndValidateCustomer(
                token, reservationUid);

        reservation.setStatus(ReservationStatus.CANCEL);

        return ReservationDto.from(reservationRepository.save(reservation));
    }

    // 파트너 예약 상세정보 조회
    public ReservationDto partnerGetReservation(String token, String reservationUid) {
        Reservation reservation = getReservationAndValidatePartner(
                token, reservationUid);

        return ReservationDto.from(reservation);
    }

    // 파트너 예약 승인
    public ReservationDto partnerReservationConfirm(String token, String reservationUid) {
        Reservation reservation = getReservationAndValidatePartner(
                token, reservationUid);

        reservation.setStatus(ReservationStatus.CONFIRM);
        Reservation savedReservation = reservationRepository.save(reservation);

        sendReservationMessageToCustomer(savedReservation);

        return ReservationDto.from(savedReservation);
    }


    // 파트너 예약 거절
    public ReservationDto partnerReservationReject(String token, String reservationUid) {
        Reservation reservation = getReservationAndValidatePartner(
                token, reservationUid);

        reservation.setStatus(ReservationStatus.DENIED);
        Reservation savedReservation = reservationRepository.save(reservation);

        sendReservationMessageToCustomer(savedReservation);

        return ReservationDto.from(savedReservation);
    }

    // 매일 00시 05분에 예약 상태 confirm인 건(도착확인 안 된 건)들 NO_SHOW로 변경
    @Transactional
    @Scheduled(cron = "${schedules.cron.check.no-show}")
    public void checkReservationNoShow() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Reservation> reservations =
                reservationRepository.findAllByStatusAndDateTimeBefore(
                        ReservationStatus.CONFIRM, yesterday.atTime(LocalTime.MAX));

        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.NO_SHOW);
            reservationRepository.save(reservation);
        }
    }

    private Reservation getReservation(String reservationUid) {
        return reservationRepository.findByUid(reservationUid)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }


    // 예약 요청 문자 발송 메서드
    private void sendReservationMessageToPartner(Reservation reservation) {
        String text = String.format("%s에 예약이 접수(수정)되었습니다.\n" +
                        "- 예약일자 : %s\n" +
                        "- 예약자명 : %s\n" +
                        "예약 상세내용 바로가기\n" +
                        "> http://localhost:8080/partner/reservation/detail?uid=%s",
                reservation.getStore().getStorename(),
                reservation.getDateTime().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                reservation.getUnderName(),
                reservation.getUid());

        sendMessageService.sendOneMessage(
                reservation.getStore().getPartner().getPhone(),
                text);
    }

    // 예약 확정 및 거절 문자 발송 메서드
    private void sendReservationMessageToCustomer(Reservation reservation) {
        String text = String.format("%s에 예약이 %s되었습니다.\n" +
                        "- 예약일자 : %s\n" +
                        "- 예약자명 : %s\n" +
                        "예약 상세내용 바로가기\n" +
                        "> http://localhost:8080/customer/reservation/detail?uid=%s",
                reservation.getStatus().getMsg(),
                reservation.getStore().getStorename(),
                reservation.getDateTime().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                reservation.getUnderName(),
                reservation.getUid());

        sendMessageService.sendOneMessage(reservation.getPhone(), text);
    }

    private Reservation getReservationAndValidateCustomer(String token, String reservationUid) {
        Reservation reservation = getReservation(reservationUid);

        if (!reservation.getCustomer().getUid().equals(tokenProvider.getUid(token))) {
            throw new CustomException(ErrorCode.ACCESS_ONLY_REQUESTED_CUSTOMER);
        }

        return reservation;
    }

    private Reservation getReservationAndValidatePartner(String token, String reservationUid) {
        Reservation reservation = getReservation(reservationUid);

        if (!reservation.getStore().getPartner().getUid().equals(
                tokenProvider.getUid(token))) {
            throw new CustomException(ErrorCode.ACCESS_ONLY_STORE_OWNER);
        }

        return reservation;
    }

}
