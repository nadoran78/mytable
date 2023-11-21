package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Partner;
import com.zerobase.mytable.domain.Store;
import com.zerobase.mytable.dto.StoreDto;
import com.zerobase.mytable.dto.StoreRegisterDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.repository.StoreRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PartnerRepository partnerRepository;

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
                .sido("경기도")
                .sigungu("군포시")
                .roadname("번영로")
                .detailAddress("행복한 곳")
                .description("맛있는 가게")
                .build();

        given(storeRepository.findByStorename(anyString()))
                .willReturn(Optional.of(store));
        //when
        StoreDto response = storeService.getStoreInfo("포장마차");
        //then
        assertEquals(response.getStorename(), "포장마차");
        assertEquals(response.getPhone(), "02-111-1111");
        assertEquals(response.getSido(), "경기도");
        assertEquals(response.getSigungu(), "군포시");
        assertEquals(response.getRoadname(), "번영로");
        assertEquals(response.getDetailAddress(), "행복한 곳");
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
        assertEquals(storeDto.getSido(), "경기도");
        assertEquals(storeDto.getSigungu(), "군포시");
        assertEquals(storeDto.getRoadname(), "번영로");
        assertEquals(storeDto.getDetailAddress(), "행복한 곳");
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

}