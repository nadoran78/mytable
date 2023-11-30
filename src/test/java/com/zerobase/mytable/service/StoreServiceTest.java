package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Partner;
import com.zerobase.mytable.domain.Reservation;
import com.zerobase.mytable.domain.Store;
import com.zerobase.mytable.dto.ReservationDto;
import com.zerobase.mytable.dto.StoreDto;
import com.zerobase.mytable.dto.StoreRegisterDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.repository.ReservationRepository;
import com.zerobase.mytable.repository.StoreRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.Address;
import com.zerobase.mytable.type.CommonResponse;
import com.zerobase.mytable.type.ErrorCode;
import com.zerobase.mytable.type.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private StoreService storeService;

    // 점포 등록 성공 테스트
    @Test
    void successRegister() {
        //given
        Partner partner = Partner.builder().build();
        Store store = Store.builder().storename("포장마차").build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.empty());
        given(partnerRepository.getByUid(anyString()))
                .willReturn(partner);
        given(tokenProvider.getUid(anyString())).willReturn("abc");
        given(storeRepository.save(any(Store.class))).willReturn(store);

        //when
        StoreRegisterDto.Request request = StoreRegisterDto.Request.builder()
                .storename("포장마차")
                .phone("02-111-1111")
                .sido("경기도")
                .sigungu("군포시")
                .roadname("번영로")
                .detailAddress("행복한 곳")
                .description("맛있는 가게")
                .build();

        StoreRegisterDto.Response response = storeService.register("abc", request);

        //then
        assertTrue(response.isSuccess());
        assertEquals(response.getCode(), 0);
        assertEquals(response.getMsg(), "요청 성공");
    }

    // 기가입된 점포 등록 테스트
    @Test
    void register_AlreadyRegisteredStorename() {
        //given
        Store store = Store.builder().storename("포장마차").build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        //when
        StoreRegisterDto.Request request = StoreRegisterDto.Request.builder()
                .storename("포장마차")
                .phone("02-111-1111")
                .sido("경기도")
                .sigungu("군포시")
                .roadname("번영로")
                .detailAddress("행복한 곳")
                .description("맛있는 가게")
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.register("abc", request));
        //then
        assertEquals(customException.getErrorCode(),
                ErrorCode.ALREADY_REGISTERED_STORENAME);
    }

    // 점포 검색어 자동완성 성공
    @Test
    void successAutoComplete() {
        //given
        Store store1 = Store.builder().storename("포장마차").build();
        Store store2 = Store.builder().storename("포장마차 2호점").build();
        Store store3 = Store.builder().storename("포장마차 3호점").build();

        given(storeRepository.findByStorenameContains(anyString(), any(Pageable.class)))
                .willReturn(List.of(store1, store2, store3));
        //when
        List<String> response = storeService.autoComplete("장");
        //then
        assertEquals(response.get(0), "포장마차");
        assertEquals(response.get(1), "포장마차 2호점");
        assertEquals(response.get(2), "포장마차 3호점");
    }

    // 점포 조회 성공
    @Test
    void successGetStoreInfo() {
        //given
        Store store = Store.builder()
                .storename("포장마차")
                .phone("02-111-1111")
                .address(Address.builder()
                        .sido("경기도")
                        .sigungu("군포시")
                        .roadname("번영로")
                        .detailAddress("행복한 곳")
                        .build())
                .description("맛있는 가게")
                .build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        //when
        StoreDto response = storeService.getStoreInfo("포장마차");
        //then
        assertEquals(response.getStorename(), "포장마차");
        assertEquals(response.getPhone(), "02-111-1111");
        assertEquals(response.getAddress().getSido(), "경기도");
        assertEquals(response.getAddress().getSigungu(), "군포시");
        assertEquals(response.getAddress().getRoadname(), "번영로");
        assertEquals(response.getAddress().getDetailAddress(), "행복한 곳");
        assertEquals(response.getDescription(), "맛있는 가게");
    }

    // 점포 조회 시 찾으려는 점포 없으면 예외 발생 테스트
    @Test
    void getStoreInfo_NotFoundStore() {
        //given
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.getStoreInfo("abc"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_STORE);
    }

    // 점포 정보 수정 성공
    @Test
    void successUpdateStoreInfo() {
        //given
        Store store = Store.builder()
                .storename("포장마차")
                .partner(Partner.builder().uid("abc").build())
                .build();

        given(storeRepository.findByStorename("aaa"))
                .willReturn(Optional.of(store));
        given(storeRepository.findByStorename("포차"))
                .willReturn(Optional.empty());
        given(tokenProvider.getUid(anyString())).willReturn("abc");
        given(storeRepository.save(any(Store.class)))
                .willReturn(store);
        //when
        StoreRegisterDto.Request request = StoreRegisterDto.Request.builder()
                .storename("포차")
                .phone("02-111-1111")
                .sido("경기도")
                .sigungu("군포시")
                .roadname("번영로")
                .detailAddress("행복한 곳")
                .description("맛있는 가게")
                .build();
        StoreDto storeDto = storeService.updateStoreInfo("aaa", "aaa", request);

        //then
        assertEquals(storeDto.getStorename(), "포차");
        assertEquals(storeDto.getPhone(), "02-111-1111");
        assertEquals(storeDto.getAddress().getSido(), "경기도");
        assertEquals(storeDto.getAddress().getSigungu(), "군포시");
        assertEquals(storeDto.getAddress().getRoadname(), "번영로");
        assertEquals(storeDto.getAddress().getDetailAddress(), "행복한 곳");
        assertEquals(storeDto.getDescription(), "맛있는 가게");
    }

    // 점포 수정 시 등록되지 않은 점포인 경우 예외 처리 테스트
    @Test
    void updateStoreInfo_NotFoundStore() {
        //given
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.empty());
        //when
        StoreRegisterDto.Request request = StoreRegisterDto.Request.builder()
                .storename("포차")
                .phone("02-111-1111")
                .sido("경기도")
                .sigungu("군포시")
                .roadname("번영로")
                .detailAddress("행복한 곳")
                .description("맛있는 가게")
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.updateStoreInfo("abc", "abc", request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_STORE);
    }

    // 점포 수정 시 점포 등록한 파트너가 아닌 경우 예외 처리
    @Test
    void updateStoreInfo_AccessDenied() {
        //given
        Store store = Store.builder()
                .storename("포장마차")
                .partner(Partner.builder().uid("abcd").build())
                .build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(tokenProvider.getUid(anyString())).willReturn("jjj");
        //when
        StoreRegisterDto.Request request = StoreRegisterDto.Request.builder()
                .storename("포차")
                .phone("02-111-1111")
                .sido("경기도")
                .sigungu("군포시")
                .roadname("번영로")
                .detailAddress("행복한 곳")
                .description("맛있는 가게")
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.updateStoreInfo("abc", "abc", request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_DENIED);
    }

    // 점포 수정 시 수정하려는 점포명이 이미 등록되어 있을 경우 예외 처리
    @Test
    void updateStoreInfo_AlreadyRegisteredStorename() {
        //given
        Store store = Store.builder()
                .storename("포장마차")
                .partner(Partner.builder().uid("abc").build())
                .build();

        given(storeRepository.findByStorename("포장마차"))
                .willReturn(Optional.of(store));
        given(storeRepository.findByStorename("포차"))
                .willReturn(Optional.of(store));
        given(tokenProvider.getUid(anyString())).willReturn("abc");

        //when
        StoreRegisterDto.Request request = StoreRegisterDto.Request.builder()
                .storename("포차")
                .phone("02-111-1111")
                .sido("경기도")
                .sigungu("군포시")
                .roadname("번영로")
                .detailAddress("행복한 곳")
                .description("맛있는 가게")
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.updateStoreInfo("abc", "포장마차", request));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ALREADY_REGISTERED_STORENAME);
    }

    // 점포 삭제 성공
    @Test
    void successDeleteStore() {
        //given
        Store store = Store.builder()
                .storename("포장마차")
                .partner(Partner.builder().uid("abc").build())
                .build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(tokenProvider.getUid(anyString())).willReturn("abc");

        //when
        StoreRegisterDto.Response response = storeService.deleteStore("abc", "포장마차");

        //then
        assertTrue(response.isSuccess());
        assertEquals(response.getCode(), 0);
        assertEquals(response.getMsg(), "요청 성공");
    }

    // 점포 삭제 시 요청한 점포명이 등록되어 있지 않을 경우 예외 처리
    @Test
    void deleteStore_NotFoundStore() {
        //given
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.deleteStore("abc", "포장마차"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_STORE);
    }

    // 삭제 요청한 점포를 등록한 파트너가 아닌 다른 파트너가 삭제 요청할 경우 예외 처리
    @Test
    void deleteStore_AccessDenied() {
        //given
        Store store = Store.builder()
                .storename("포장마차")
                .partner(Partner.builder().uid("abcd").build())
                .build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(tokenProvider.getUid(anyString())).willReturn("jjj");
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.deleteStore("abc", "abc"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_DENIED);
    }

    // 점주가 관리하는 점포 리스트 조회
    @Test
    void successGetMyStores() {
        //given
        Partner partner = mock(Partner.class);
        List<Store> stores = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Store store = Store.builder()
                    .storename(i + "호점")
                    .build();
            store.setCreatedAt(LocalDateTime.now().minusDays(i));
            stores.add(store);
        }

        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(partnerRepository.getByUid(anyString()))
                .thenReturn(partner);
        Mockito.when(partner.getStores()).thenReturn(stores);
        //when
        String token = "abc";
        Page<StoreDto> page = storeService.getMyStores(token, PageRequest.of(0, 5));
        //then
        assertEquals(page.getContent().get(0).getStorename(), "3호점");
        assertEquals(page.getContent().get(1).getStorename(), "2호점");
        assertEquals(page.getContent().get(2).getStorename(), "1호점");
    }

    // 파트너 점포 별 예약 조회 성공 테스트
    @Test
    void successGetReservationsByStore() {
        //given
        Store store = mock(Store.class);
        Partner partner = Partner.builder().uid("abc").build();
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = new ArrayList<>();
        int days = 1;
        for (int i = 1; i <= 4; i++) {
            if (i == 3) {
                days--;
            }
            Reservation reservation = Reservation.builder()
                    .uid(Integer.toString(i))
                    .dateTime(now.minusDays(days++))
                    .underName("홍길동")
                    .phone("123")
                    .store(store)
                    .build();
            reservation.setCreatedAt(now.minusMonths(1).plusDays(i));
            reservations.add(reservation);
        }

        Mockito.when(storeRepository.findByStorename(anyString()))
                .thenReturn(Optional.of(store));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("abc");
        Mockito.when(store.getReservations()).thenReturn(reservations);
        Mockito.when(store.getPartner()).thenReturn(partner);
        Mockito.when(store.getStorename()).thenReturn("포차");
        //when
        Page<ReservationDto> page = storeService.getReservationsByStore(
                "111", "포장마차", now.toLocalDate().minusMonths(1),
                now.toLocalDate().plusMonths(1),
                PageRequest.of(0, 5));
        //then
        assertEquals(page.getContent().get(0).getUid(), "4");
        assertEquals(page.getContent().get(1).getUid(), "2");
        assertEquals(page.getContent().get(2).getUid(), "3");
        assertEquals(page.getContent().get(3).getUid(), "1");
    }

    // 파트너 점포 별 예약 조회 시 해당 점포 없을 경우 예외 처리
    @Test
    void getReservationsByStore_NotFoundStore() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Mockito.when(storeRepository.findByStorename(anyString()))
                .thenReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.getReservationsByStore(
                        "111", "포장마차", now.toLocalDate().minusMonths(1),
                        now.toLocalDate().plusMonths(1),
                        PageRequest.of(0, 5)));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_STORE);
    }

    // 파트너 점포 별 예약 조회 시 조회하려는 파트너와 해당 점주가 다를 경우 예외 처리
    @Test
    void getReservationsByStore_AccessDenied() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Partner partner = Partner.builder().uid("abc").build();
        Store store = Store.builder().partner(partner).build();

        Mockito.when(storeRepository.findByStorename(anyString()))
                .thenReturn(Optional.of(store));
        Mockito.when(tokenProvider.getUid(anyString()))
                .thenReturn("123");
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.getReservationsByStore(
                        "111", "포장마차", now.toLocalDate().minusMonths(1),
                        now.toLocalDate().plusMonths(1),
                        PageRequest.of(0, 5)));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_DENIED);
    }

    // 파트너 키오스크에서 예약자명과 전화번호를 통해 예약 검색 성공 테스트
    @Test
    void successSearchReservation() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Partner partner = Partner.builder().uid("123").build();
        Store store = Store.builder()
                .storename("포차")
                .partner(partner).build();

        List<Reservation> reservations = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Reservation reservation = Reservation.builder()
                    .uid(Integer.toString(i))
                    .dateTime(now.minusDays(i))
                    .underName("홍길동")
                    .phone("123")
                    .store(store)
                    .build();
            reservations.add(reservation);
        }
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(tokenProvider.getUid(anyString()))
                .willReturn("123");
        given(reservationRepository.findAllByUnderNameAndPhoneAndStoreAndStatus(
                "홍길동", "123", store, ReservationStatus.CONFIRM))
                .willReturn(reservations);
        //when
        Page<ReservationDto> page = storeService.searchReservation(
                "abc", "포차", "홍길동",
                "123", PageRequest.of(0, 5));
        //then
        assertEquals(page.getContent().get(0).getUid(), "3");
        assertEquals(page.getContent().get(1).getUid(), "2");
        assertEquals(page.getContent().get(2).getUid(), "1");
    }

    // 파트너 키오스크에서 예약 검색 시 없는 점포일 경우 예외처리
    @Test
    void searchReservation_NotFoundStore() {
        //given
        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.searchReservation(
                "abc", "포차", "홍길동",
                "123", PageRequest.of(0, 5)));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_STORE);
    }

    // 파트너 키오스크에서 예약 검색 시 점주와 예약검색 요청한 파트너가 다를 경우 예외처리
    @Test
    void searchReservation_AccessDenied() {
        //given
        Partner partner = Partner.builder().uid("123").build();
        Store store = Store.builder().partner(partner).build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.searchReservation(
                        "abc", "포차", "홍길동",
                        "123", PageRequest.of(0, 5)));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_DENIED);
    }

    // 파트너 키오스크에서 예약 검색 시 조회되는 예약 없을 경우 예외 처리
    @Test
    void searchReservation_NotFoundReservation() {
        //given
        Partner partner = Partner.builder().uid("123").build();
        Store store = Store.builder()
                .storename("포차")
                .partner(partner).build();

        List<Reservation> reservations = new ArrayList<>();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        given(tokenProvider.getUid(anyString()))
                .willReturn("123");
        given(reservationRepository.findAllByUnderNameAndPhoneAndStoreAndStatus(
                "홍길동", "123", store, ReservationStatus.CONFIRM))
                .willReturn(reservations);
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.searchReservation(
                        "abc", "포차", "홍길동",
                        "123", PageRequest.of(0, 5)));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_RESERVATION);
    }

    // 파트너 키오스크에서 예약 도착 확인 성공 테스트
    @Test
    void successArrivalConfirm() {
        //given
        Partner partner = Partner.builder().uid("123").build();
        Store store = Store.builder()
                .storename("포차")
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .uid("1")
                .dateTime(LocalDateTime.now().plusMinutes(5))
                .underName("홍길동")
                .phone("123")
                .store(store)
                .build();

        given(reservationRepository.findByUid(anyString()))
                .willReturn(Optional.of(reservation));
        given(tokenProvider.getUid(anyString()))
                .willReturn("123");
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .then(returnsFirstArg());
        //when
        CommonResponse commonResponse = storeService.arrivalConfirm(
                "123", "123");
        //then
        assertEquals(commonResponse, CommonResponse.SUCCESS);
    }

    // 파트너 키오스크에서 예약 도착 확인 시 해당 예약 건 없을 때 예외 처리
    @Test
    void arrivalConfirm_NotFoundReservation() {
        //given
        given(reservationRepository.findByUid(anyString()))
                .willReturn(Optional.empty());
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.arrivalConfirm("123", "123"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.NOT_FOUND_RESERVATION);
    }

    // 파트너 키오스크에서 예약 도착 확인 시 점주와 예약 확정 요청하는 파트너가 다를 경우 예외 처리
    @Test
    void arrivalConfirm_AccessDenied() {
        //given
        Partner partner = Partner.builder().uid("123").build();
        Store store = Store.builder()
                .storename("포차")
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .uid("1")
                .dateTime(LocalDateTime.now().plusMinutes(5))
                .underName("홍길동")
                .phone("123")
                .store(store)
                .build();

        given(reservationRepository.findByUid(anyString()))
                .willReturn(Optional.of(reservation));
        given(tokenProvider.getUid(anyString()))
                .willReturn("abc");
        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.arrivalConfirm("123", "123"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ACCESS_DENIED);
    }

    // 파트너 키오스크에서 예약 도착 확인 시 도착 확인 요청 시간이 예약시간 10분 내가 아닌 경우 예외 처리
    @Test
    void arrivalConfirm_EntranceNotOnTime() {
        //given
        Partner partner = Partner.builder().uid("123").build();
        Store store = Store.builder()
                .storename("포차")
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .uid("1")
                .dateTime(LocalDateTime.now().plusMinutes(15))
                .underName("홍길동")
                .phone("123")
                .store(store)
                .build();

        given(reservationRepository.findByUid(anyString()))
                .willReturn(Optional.of(reservation));
        given(tokenProvider.getUid(anyString()))
                .willReturn("123");

        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.arrivalConfirm("123", "123"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.ENTRANCE_NOT_ON_TIME);
    }

    // 파트너 키오스크에서 예약 도착 확인 시 도착 확인 요청 시간이 예약시간 10분 경과한 경우 예외 처리
    @Test
    void arrivalConfirm_TimeOver() {
        //given
        Partner partner = Partner.builder().uid("123").build();
        Store store = Store.builder()
                .storename("포차")
                .partner(partner).build();
        Reservation reservation = Reservation.builder()
                .uid("1")
                .dateTime(LocalDateTime.now().minusMinutes(15))
                .underName("홍길동")
                .phone("123")
                .store(store)
                .build();

        given(reservationRepository.findByUid(anyString()))
                .willReturn(Optional.of(reservation));
        given(tokenProvider.getUid(anyString()))
                .willReturn("123");

        //when
        CustomException customException = assertThrows(CustomException.class,
                () -> storeService.arrivalConfirm("123", "123"));
        //then
        assertEquals(customException.getErrorCode(), ErrorCode.TIME_OVER);
    }
}


