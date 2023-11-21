package com.zerobase.mytable.dto;

import com.zerobase.mytable.domain.Store;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreDto {

    private String storename;

    private String phone;

    private String sido;

    private String sigungu;

    private String roadname;

    private String detailAddress;

    private String description;

    public static StoreDto from(Store store){
        return StoreDto.builder()
                .storename(store.getStorename())
                .phone(store.getPhone())
                .sido(store.getSido())
                .sigungu(store.getSigungu())
                .roadname(store.getRoadname())
                .detailAddress(store.getDetailAddress())
                .description(store.getDescription())
                .build();
    }

}
