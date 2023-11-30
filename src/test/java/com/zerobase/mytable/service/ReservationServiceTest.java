package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Partner;
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
import net.nurigo.sdk.message.model.MessageType;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private SendMessageService sendMessageService;
    @InjectMocks
    private ReservationService reservationService;

    // 예약 요청 성공 테스트
    @Test
    void successMakeReservation() {
        //given
        Partner partner = Partner.builder().phone("010-1111-1111").build();
        Store store = Store.builder().partner(partner).build();
        Customer customer = Customer.builder().build();
        SingleMessageSentResponse singleMessageSentResponse =
                new SingleMessageSentResponse("a", "b", "c",
                        MessageType.MMS, "1", "2", "3",
                        "4", "5");

        Mockito.when(storeRepository.findByStorename(anyString()))
                .thenReturn(Optional.of(store));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(customerRepository.getByUid(anyString()))
                .thenReturn(customer);
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .then(returnsFirstArg());
        Mockito.when(sendMessageService.sendOneMessage(anyString(), anyString()))
                .thenReturn(singleMessageSentResponse);
        //when
        String token = "abc";
        ReservationDto request = ReservationDto.builder()
                .date(LocalDate.of(2023, 12, 1))
                .time(LocalTime.of(16, 30))
                .underName("홍길동")
                .phone("010-1111-1111")
                .storename("포장마차 1호점")
                .build();
        CommonResponse response = reservationService.makeReservation(token, request);
        //then
        assertEquals(response, CommonResponse.SUCCESS);
    }

    // 예약 요청 시 해당 점포 없을 경우 예외처리
    @Test
    void makeReservation_NotFoundStore() {
        //given
        Mockito.when(storeRepository.findByStorename(anyString()))
                .thenReturn(Optional.empty());
        //when
        String token = "abc";
        ReservationDto request = ReservationDto.builder()
                .date(LocalDate.of(2023, 12, 1))
                .time(LocalTime.of(16, 30))
                .underName("홍길동")
                .phone("010-1111-1111")
                .storename("포장마차 1호점")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.makeReservation(token, request));
        //then
        assertEquals(ErrorCode.NOT_FOUND_STORE, customException.getErrorCode());
    }

    // 예약 요청 시 예약 시간이 현재 시간으로부터 한달을 경과할 경우 예외 처리
    @Test
    void makeReservation_ReservationDateMustBeInAMonth() {
        //given
        Store store = Store.builder().build();
        Mockito.when(storeRepository.findByStorename(anyString()))
                .thenReturn(Optional.of(store));
        //when
        String token = "abc";
        ReservationDto request = ReservationDto.builder()
                .date(LocalDate.now().plusMonths(2))
                .time(LocalTime.of(16, 30))
                .underName("홍길동")
                .phone("010-1111-1111")
                .storename("포장마차 1호점")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.makeReservation(token, request));
        //then
        assertEquals(customException.getErrorCode(),
                ErrorCode.RESERVATION_DATE_MUST_BE_IN_A_MONTH);
    }

    // 고객이 예약한 예약 리스트 조회 성공 테스트
    @Test
    void customerGetMyReservations() {
        //given
        Customer customer = mock(Customer.class);
        Store store = Store.builder().storename("포장마차").build();

        List<Reservation> reservations = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            LocalDate date = LocalDate.of(2023, 12, i);
            Reservation reservation = Reservation.builder()
                    .customer(customer)
                    .uid(Integer.toString(i))
                    .dateTime(LocalDateTime.of(date, LocalTime.MAX))
                    .underName("abc")
                    .phone("123")
                    .store(store)
                    .build();
            reservations.add(reservation);
        }

        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(customerRepository.getByUid(anyString()))
                .thenReturn(customer);
        Mockito.when(customer.getReservations()).thenReturn(reservations);
        //when
        String token = "abc";
        Page<ReservationDto> page = reservationService.customerGetMyReservations(
                token, PageRequest.of(0, 5));
        //then
        for (int i = 0; i < reservations.size(); i++) {
            assertEquals(page.getContent().get(i).getUid(),
                    reservations.stream()
                            .map(ReservationDto::from)
                            .collect(Collectors.toList()).get(i).getUid());
        }
    }

    // 고객 예약 상세정보 조회
    @Test
    void successCustomerGetReservation() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder().uid("abc").build();
        Store store = Store.builder().storename("포장마차").build();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .uid("1")
                .dateTime(now)
                .underName("abc")
                .phone("123")
                .store(store)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        //when
        String token = "1111";
        String reservationUid = "1";
        ReservationDto reservationDto = reservationService.customerGetReservation(
                token, reservationUid);
        //then
        assertEquals(reservationDto.getUid(), "1");
        assertEquals(reservationDto.getDate(), now.toLocalDate());
        assertEquals(reservationDto.getTime(), now.toLocalTime());
        assertEquals(reservationDto.getUnderName(), "abc");
        assertEquals(reservationDto.getPhone(), "123");
        assertEquals(reservationDto.getStorename(), "포장마차");
    }

    // 고객 예약 상세정보 조회 시 찾고자 하는 예약 없을 때 예외 처리
    @Test
    void customerGetReservation_ReservationNotFound() {
        //given
        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.empty());

        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.customerGetReservation(token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.RESERVATION_NOT_FOUND);
    }

    // 고객 예약 상세정보 조회 시 예약요청 고객이 조회하려는 고객과 다른 경우 예외 처리
    @Test
    void customerGetReservation_AccessOnlyRequestedCustomer() {
        //given
        Customer customer = Customer.builder().uid("abc").build();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("aaa");
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.customerGetReservation(token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(),
                ErrorCode.ACCESS_ONLY_REQUESTED_CUSTOMER);
    }

    // 고객 예약 수정 성공 테스트
    @Test
    void successUpdateReservation() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder().uid("abc").build();
        Partner partner = Partner.builder().phone("123").build();
        Store store = Store.builder()
                .storename("포장마차").partner(partner).build();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .uid("1")
                .dateTime(now)
                .underName("abc")
                .phone("123")
                .store(store)
                .build();

        SingleMessageSentResponse singleMessageSentResponse =
                new SingleMessageSentResponse("a", "b", "c",
                        MessageType.MMS, "1", "2", "3",
                        "4", "5");

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .then(returnsFirstArg());
        Mockito.when(sendMessageService.sendOneMessage(anyString(), anyString()))
                .thenReturn(singleMessageSentResponse);
        //when
        LocalDate tomorrow = now.toLocalDate().plusDays(1);

        String token = "111";
        String reservationUid = "123123";
        ReservationDto request = ReservationDto.builder()
                .date(tomorrow)
                .time(LocalTime.of(16, 30))
                .underName("홍길동")
                .phone("010-1111-1111")
                .storename("포장마차")
                .build();
        ReservationDto reservationDto = reservationService.updateReservation(
                token, reservationUid, request);
        //then
        assertEquals(reservationDto.getDate(), tomorrow);
        assertEquals(reservationDto.getTime(), LocalTime.of(16, 30));
        assertEquals(reservationDto.getUnderName(), "홍길동");
        assertEquals(reservationDto.getPhone(), "010-1111-1111");
        assertEquals(reservationDto.getStorename(), "포장마차");
        assertEquals(reservationDto.getStatus(), ReservationStatus.WAITING);
    }

    // 고객 예약 수정 시 찾고자 하는 예약 없을 때 예외 처리
    @Test
    void updateReservation_ReservationNotFound() {
        //given
        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.empty());

        //when
        String token = "1111";
        String reservationUid = "1";
        ReservationDto request = ReservationDto.builder()
                .date(LocalDate.now())
                .time(LocalTime.of(16, 30))
                .underName("홍길동")
                .phone("010-1111-1111")
                .storename("포장마차")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.updateReservation(
                        token, reservationUid, request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.RESERVATION_NOT_FOUND);
    }

    // 고객 예약 수정 시 예약요청 고객이 수정하려는 고객과 다른 경우 예외 처리
    @Test
    void updateReservation_AccessOnlyRequestedCustomer() {
        //given
        Customer customer = Customer.builder().uid("abc").build();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("aaa");
        //when
        String token = "1111";
        String reservationUid = "1";
        ReservationDto request = ReservationDto.builder()
                .date(LocalDate.now())
                .time(LocalTime.of(16, 30))
                .underName("홍길동")
                .phone("010-1111-1111")
                .storename("포장마차")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.updateReservation(
                        token, reservationUid, request));
        //then
        assertEquals(customException.getErrorCode(),
                ErrorCode.ACCESS_ONLY_REQUESTED_CUSTOMER);
    }

    // 예약 수정 시 점포명 변경하려고 할 경우 예외 처리
    @Test
    void updateReservation_CannotUpdateStore() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder().uid("abc").build();
        Partner partner = Partner.builder().phone("123").build();
        Store store = Store.builder()
                .storename("포장마차").partner(partner).build();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .uid("1")
                .dateTime(now)
                .underName("abc")
                .phone("123")
                .store(store)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");

        //when
        LocalDate tomorrow = now.toLocalDate().plusDays(1);

        String token = "111";
        String reservationUid = "123123";
        ReservationDto request = ReservationDto.builder()
                .date(tomorrow)
                .time(LocalTime.of(16, 30))
                .underName("홍길동")
                .phone("010-1111-1111")
                .storename("포장마차 2호점")
                .build();
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.updateReservation(
                        token, reservationUid, request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.CANNOT_UPDATE_STORE);
    }

    // 고객 예약 취소 성공 테스트
    @Test
    void successCancelReservation() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder().uid("abc").build();
        Store store = Store.builder().storename("포장마차").build();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .uid("1")
                .dateTime(now)
                .underName("abc")
                .phone("123")
                .store(store)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .then(returnsFirstArg());
        //when
        String token = "1111";
        String reservationUid = "1";
        ReservationDto reservationDto = reservationService.cancelReservation(
                token, reservationUid);
        //then
        assertEquals(reservationDto.getUid(), "1");
        assertEquals(reservationDto.getDate(), now.toLocalDate());
        assertEquals(reservationDto.getTime(), now.toLocalTime());
        assertEquals(reservationDto.getUnderName(), "abc");
        assertEquals(reservationDto.getPhone(), "123");
        assertEquals(reservationDto.getStorename(), "포장마차");
        assertEquals(reservationDto.getStatus(), ReservationStatus.CANCEL);
    }

    // 고객 예약 삭제 시 찾고자 하는 예약 없을 때 예외 처리
    @Test
    void cancelReservation_ReservationNotFound() {
        //given
        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.empty());

        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.cancelReservation(token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.RESERVATION_NOT_FOUND);
    }

    // 고객 예약 상세정보 조회 시 예약요청 고객이 조회하려는 고객과 다른 경우 예외 처리
    @Test
    void cancelReservation_AccessOnlyRequestedCustomer() {
        //given
        Customer customer = Customer.builder().uid("abc").build();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("aaa");
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.cancelReservation(token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(),
                ErrorCode.ACCESS_ONLY_REQUESTED_CUSTOMER);
    }

    // 파트너 예약 상세정보 조회 성공 테스트
    @Test
    void successPartnerGerReservation() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Partner partner = Partner.builder().uid("abc").build();
        Store store = Store.builder()
                .storename("포장마차")
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .uid("1")
                .dateTime(now)
                .underName("abc")
                .phone("123")
                .store(store)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        //when
        String token = "1111";
        String reservationUid = "1";
        ReservationDto reservationDto = reservationService.partnerGetReservation(
                token, reservationUid);
        //then
        assertEquals(reservationDto.getUid(), "1");
        assertEquals(reservationDto.getDate(), now.toLocalDate());
        assertEquals(reservationDto.getTime(), now.toLocalTime());
        assertEquals(reservationDto.getUnderName(), "abc");
        assertEquals(reservationDto.getPhone(), "123");
        assertEquals(reservationDto.getStorename(), "포장마차");
    }

    // 파트너 예약 상세정보 조회 시 찾는 예약 없을 경우 예외 처리 테스트
    @Test
    void partnerGerReservation_ReservationNotFound() {
        //given
        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.empty());
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.partnerGetReservation(token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.RESERVATION_NOT_FOUND);
    }

    // 파트너 예약 상세정보 조회 시 점주와 조회하려는 파트너가 다른 경우 예외 처리
    @Test
    void partnerGerReservation_AccessOnlyStoreOwner() {
        //given
        Partner partner = Partner.builder().uid("abc").build();
        Store store = Store.builder()
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .store(store)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("aaa");
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.partnerGetReservation(token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_ONLY_STORE_OWNER);
    }

    // 파트너 예약 승인 성공 테스트
    @Test
    void successPartnerReservationConfirm() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Partner partner = Partner.builder().uid("abc").phone("1234").build();
        Store store = Store.builder()
                .storename("포장마차")
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .uid("1")
                .dateTime(now)
                .underName("abc")
                .phone("123")
                .store(store)
                .build();
        SingleMessageSentResponse singleMessageSentResponse =
                new SingleMessageSentResponse("a", "b", "c",
                        MessageType.MMS, "1", "2", "3",
                        "4", "5");

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .then(returnsFirstArg());
        Mockito.when(sendMessageService.sendOneMessage(anyString(), anyString()))
                .thenReturn(singleMessageSentResponse);
        //when
        String token = "1111";
        String reservationUid = "1";
        ReservationDto reservationDto = reservationService.partnerReservationConfirm(
                token, reservationUid);
        //then
        assertEquals(reservationDto.getUid(), "1");
        assertEquals(reservationDto.getDate(), now.toLocalDate());
        assertEquals(reservationDto.getTime(), now.toLocalTime());
        assertEquals(reservationDto.getUnderName(), "abc");
        assertEquals(reservationDto.getPhone(), "123");
        assertEquals(reservationDto.getStorename(), "포장마차");
        assertEquals(reservationDto.getStatus(), ReservationStatus.CONFIRM);
    }

    // 파트너 예약 승인 시 찾는 예약 없을 경우 예외 처리 테스트
    @Test
    void partnerReservationConfirm_ReservationNotFound() {
        //given
        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.empty());
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.partnerReservationConfirm(
                        token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.RESERVATION_NOT_FOUND);
    }

    // 파트너 예약 승인 시 점주와 조회하려는 파트너가 다른 경우 예외 처리
    @Test
    void partnerReservationConfirm_AccessOnlyStoreOwner() {
        //given
        Partner partner = Partner.builder().uid("abc").build();
        Store store = Store.builder()
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .store(store)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("aaa");
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.partnerReservationConfirm(
                        token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_ONLY_STORE_OWNER);
    }

    // 파트너 예약 거절 성공 테스트
    @Test
    void successPartnerReservationReject() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Partner partner = Partner.builder().uid("abc").phone("1234").build();
        Store store = Store.builder()
                .storename("포장마차")
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .uid("1")
                .dateTime(now)
                .underName("abc")
                .phone("123")
                .store(store)
                .build();
        SingleMessageSentResponse singleMessageSentResponse =
                new SingleMessageSentResponse("a", "b", "c",
                        MessageType.MMS, "1", "2", "3",
                        "4", "5");

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .then(returnsFirstArg());
        Mockito.when(sendMessageService.sendOneMessage(anyString(), anyString()))
                .thenReturn(singleMessageSentResponse);
        //when
        String token = "1111";
        String reservationUid = "1";
        ReservationDto reservationDto = reservationService.partnerReservationReject(
                token, reservationUid);
        //then
        assertEquals(reservationDto.getUid(), "1");
        assertEquals(reservationDto.getDate(), now.toLocalDate());
        assertEquals(reservationDto.getTime(), now.toLocalTime());
        assertEquals(reservationDto.getUnderName(), "abc");
        assertEquals(reservationDto.getPhone(), "123");
        assertEquals(reservationDto.getStorename(), "포장마차");
        assertEquals(reservationDto.getStatus(), ReservationStatus.DENIED);
    }

    // 파트너 예약 거절 시 찾는 예약 없을 경우 예외 처리 테스트
    @Test
    void partnerReservationReject_ReservationNotFound() {
        //given
        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.empty());
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.partnerReservationReject(
                        token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.RESERVATION_NOT_FOUND);
    }

    // 파트너 예약 승인 시 점주와 조회하려는 파트너가 다른 경우 예외 처리
    @Test
    void partnerReservationReject_AccessOnlyStoreOwner() {
        //given
        Partner partner = Partner.builder().uid("abc").build();
        Store store = Store.builder()
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .store(store)
                .build();

        Mockito.when(reservationRepository.findByUid(anyString()))
                .thenReturn(Optional.of(reservation));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("aaa");
        //when
        String token = "1111";
        String reservationUid = "1";
        CustomException customException = assertThrows(CustomException.class,
                () -> reservationService.partnerReservationReject(
                        token, reservationUid));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_ONLY_STORE_OWNER);
    }

    // 매일 00시 05분에 예약 상태 confirm인 건(도착확인 안 된 건)들 NO_SHOW로 변경 성공 테스트
    @Test
    void successCheckReservationNoShow() {
        //given
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            reservations.add(Reservation.builder()
                    .uid(Integer.toString(i))
                    .status(ReservationStatus.CONFIRM)
                    .dateTime(now)
                    .build());
        }

        Mockito.when(reservationRepository.findAllByStatusAndDateTimeBefore(
                any(), any()))
                .thenReturn(reservations);
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .then(returnsFirstArg());

        //when
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.checkReservationNoShow();
        //then
        verify(reservationRepository, times(3))
                .save(captor.capture());
        List<Reservation> captureList = captor.getAllValues();
        assertEquals(captureList.get(0).getUid(), "0") ;
        assertEquals(captureList.get(1).getUid(), "1") ;
        assertEquals(captureList.get(2).getUid(), "2") ;
        assertEquals(captureList.get(0).getStatus(), ReservationStatus.NO_SHOW) ;
        assertEquals(captureList.get(1).getStatus(), ReservationStatus.NO_SHOW) ;
        assertEquals(captureList.get(2).getStatus(), ReservationStatus.NO_SHOW) ;
    }

}