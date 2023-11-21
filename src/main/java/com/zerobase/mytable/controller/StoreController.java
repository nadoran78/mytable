package com.zerobase.mytable.controller;

import com.zerobase.mytable.dto.StoreDto;
import com.zerobase.mytable.dto.StoreRegisterDto;
import com.zerobase.mytable.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    // 점포 등록
    @PostMapping("/register")
    @PreAuthorize("hasRole('PARTNER')")
    public StoreRegisterDto.Response storeRegister(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @Valid @RequestBody StoreRegisterDto.Request request) {
        return storeService.register(token, request);
    }

    // 점포 검색 시 10개 자동완성
    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String keyword) {
        return storeService.autoComplete(keyword);
    }

    // 점포 정보 조회
    @GetMapping("/info")
    public StoreDto getStoreInfo(@RequestParam String storename) {
        return storeService.getStoreInfo(storename);
    }

    // 점포 정보 수정
    @PutMapping("/info/update")
    @PreAuthorize("hasRole('PARTNER')")
    public StoreDto updateStoreInfo(@RequestHeader(name = "X-AUTH-TOKEN") String token,
                                    @RequestParam String existingStorename,
                                    @Valid @RequestBody StoreRegisterDto.Request updateRequest) {
        return storeService.updateStoreInfo(token, existingStorename, updateRequest);
    }

    // 점포 삭제
    @DeleteMapping("/info/delete")
    @PreAuthorize("hasRole('PARTNER')")
    public StoreRegisterDto.Response deleteStore(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestParam String storename) {
        return storeService.deleteStore(token, storename);
    }
}
