package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.BaseEntity;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final PartnerRepository partnerRepository;
    private final ReservationRepository reservationRepository;
    private final TokenProvider tokenProvider;

    // 점포 등록
    public StoreRegisterDto.Response register(String token, StoreRegisterDto.Request request) {
        if (storeRepository.findByStorename(request.getStorename()).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_STORENAME);
        }

        Store requestStore = Store.from(request);
        requestStore.setPartner(partnerRepository.getByUid(tokenProvider.getUid(token)));

        Store savedStore = storeRepository.save(requestStore);

        StoreRegisterDto.Response response = new StoreRegisterDto.Response();

        if (!savedStore.getStorename().isEmpty()) {
            setSuccessResult(response);
        } else {
            setFailResult(response);
        }

        return response;
    }

    // 점포 검색 시 키워드 포함하는 10개 검색어 자동완성
    public List<String> autoComplete(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        List<Store> stores = storeRepository.findByStorenameContains(keyword, limit);
        return stores.stream()
                .map(Store::getStorename)
                .collect(Collectors.toList());
    }

    // 점포 정보 조회
    public StoreDto getStoreInfo(String storename) {
        return StoreDto.from(getStore(storename));
    }

    // 점포 정보 수정(점포에 해당하는 파트너 일치 여부 확인 후 처리)
    public StoreDto updateStoreInfo(String token, String existingStorename,
                                    StoreRegisterDto.Request updateRequest) {
        Store store = getStore(existingStorename);

        partnerValidate(token, store);

        if (storeRepository.findByStorename(updateRequest.getStorename()).isPresent()
                && !updateRequest.getStorename().equals(existingStorename)) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_STORENAME);
        }

        store.setStorename(updateRequest.getStorename());
        store.setPhone(updateRequest.getPhone());
        store.setAddress(Address.builder()
                .sido(updateRequest.getSido())
                .sigungu(updateRequest.getSigungu())
                .roadname(updateRequest.getRoadname())
                .detailAddress(updateRequest.getDetailAddress())
                .build());
        store.setDescription(updateRequest.getDescription());

        return StoreDto.from(storeRepository.save(store));
    }

    // 점포 삭제
    public StoreRegisterDto.Response deleteStore(String token, String storename) {
        Store store = getStore(storename);

        partnerValidate(token, store);

        storeRepository.delete(store);

        StoreRegisterDto.Response response = new StoreRegisterDto.Response();
        setSuccessResult(response);

        return response;
    }

    // 파트너에 해당하는 점포 리스트 조회
    public Page<StoreDto> getMyStores(String token, PageRequest pageRequest) {
        Partner partner = partnerRepository.getByUid(tokenProvider.getUid(token));

        List<StoreDto> storeDtos = partner.getStores().stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .map(StoreDto::from)
                .collect(Collectors.toList());

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), storeDtos.size());

        return new PageImpl<>(
                storeDtos.subList(start, end),
                pageRequest,
                storeDtos.size());
    }

    // 파트너 점포 별 예약 조회
    // 1. 현재 시간 이후의 예약 목록만 줄 것
    // 2. 예약 시간 별 구분해서 반환할 것
    public Page<ReservationDto> getReservationsByStore(
            String token,
            String storename,
            LocalDate startDate,
            LocalDate endDate,
            PageRequest pageRequest) {
        Store store = storeRepository.findByStorename(storename)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        partnerValidate(token, store);

        // 1. reservation의 날짜로 필터링
        // 2. reservation 예약 일자 별로 오름차순 정렬
        // 3. reservation 예약 일자 같을 경우 생성 시간 별로 오름차순 정렬
        List<ReservationDto> reservationDtos = store.getReservations().stream()
                .filter(x -> x.getDateTime().isAfter(startDate.atStartOfDay())
                        && x.getDateTime().isBefore(endDate.atTime(LocalTime.MAX)))
                .sorted((x, y) -> {
                    if (x.getDateTime().isBefore(y.getDateTime())) {
                        return -1;
                    } else if (x.getDateTime().isEqual(y.getDateTime())) {
                        if (x.getCreatedAt().isBefore(y.getCreatedAt())) {
                            return -1;
                        } else if (x.getCreatedAt().isEqual(y.getCreatedAt())) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        return 1;
                    }
                })
                .map(ReservationDto::from)
                .collect(Collectors.toList());

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), reservationDtos.size());

        return new PageImpl<>(
                reservationDtos.subList(start, end),
                pageRequest,
                reservationDtos.size());
    }

    // 파트너 키오스크에서 예약자명과 전화번호를 통해 예약 검색
    public Page<ReservationDto> searchReservation(String token,
                                            String storename,
                                            String underName,
                                            String phone,
                                            PageRequest pageRequest) {
        Store store = storeRepository.findByStorename(storename)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));

        // 점포의 파트너와 키오스크에 접속한 파트너가 다른 경우 예외 처리
        partnerValidate(token, store);

        List<ReservationDto> reservationDtos =
                reservationRepository.findAllByUnderNameAndPhoneAndStoreAndStatus(
                        underName, phone, store, ReservationStatus.CONFIRM).stream()
                        .sorted(Comparator.comparing(Reservation::getDateTime))
                        .map(ReservationDto::from)
                        .collect(Collectors.toList());

        if (reservationDtos.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_RESERVATION);
        }

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), reservationDtos.size());

        return new PageImpl<>(
                reservationDtos.subList(start, end),
                pageRequest,
                reservationDtos.size());
    }

    // 파트너 키오스크에서 예약 도착 확인
    public CommonResponse arrivalConfirm(String token, String reservationUid) {
        Reservation reservation = reservationRepository.findByUid(reservationUid)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_FOUND_RESERVATION));

        partnerValidate(token, reservation.getStore());

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(reservation.getDateTime().minusMinutes(10L))) {
            throw new CustomException(ErrorCode.ENTRANCE_NOT_ON_TIME);
        } else if (now.isAfter(reservation.getDateTime().plusMinutes(10L))) {
            throw new CustomException(ErrorCode.TIME_OVER);
        }

        reservation.setStatus(ReservationStatus.ARRIVED);
        reservationRepository.save(reservation);

        return CommonResponse.SUCCESS;
    }


    private Store getStore(String storename) {
        return storeRepository.findByStorename(storename)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STORE));
    }


    private void setSuccessResult(StoreRegisterDto.Response response) {
        response.setSuccess(true);
        response.setCode(0);
        response.setMsg("요청 성공");
    }

    private void setFailResult(StoreRegisterDto.Response response) {
        response.setSuccess(false);
        response.setCode(-1);
        response.setMsg("요청 실패");
    }

    private void partnerValidate(String token, Store store) {
        if (!store.getPartner().getUid().equals(tokenProvider.getUid(token))) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
