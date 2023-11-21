package com.zerobase.mytable.service;

import com.zerobase.mytable.domain.Store;
import com.zerobase.mytable.dto.StoreDto;
import com.zerobase.mytable.dto.StoreRegisterDto;
import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.repository.PartnerRepository;
import com.zerobase.mytable.repository.StoreRepository;
import com.zerobase.mytable.security.TokenProvider;
import com.zerobase.mytable.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StoreService {

    private StoreRepository storeRepository;
    private PartnerRepository partnerRepository;
    private TokenProvider tokenProvider;

    // 점포 등록
    public StoreRegisterDto.Response register(String token, StoreRegisterDto.Request request) {
        if (storeRepository.findByStorename(request.getStorename()).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_STORENAME);
        }

        Store requestStore = Store.from(request);
        requestStore.setPartner(partnerRepository.getByUid(tokenProvider.getUid(token)));

        Store savedStore = storeRepository.save(requestStore);

        StoreRegisterDto.Response response = new StoreRegisterDto.Response();

        if (!savedStore.getStorename().isEmpty()){
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

        if (storeRepository.findByStorename(updateRequest.getStorename()).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_STORENAME);
        }

        store.setStorename(updateRequest.getStorename());
        store.setPhone(updateRequest.getPhone());
        store.setSido(updateRequest.getSido());
        store.setSigungu(updateRequest.getSigungu());
        store.setRoadname(updateRequest.getRoadname());
        store.setDetailAddress(updateRequest.getDetailAddress());
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

    private void partnerValidate(String token, Store store){
        if (!store.getPartner().getUid().equals(tokenProvider.getUid(token))) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
