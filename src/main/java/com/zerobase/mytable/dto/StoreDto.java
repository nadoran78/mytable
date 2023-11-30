package com.zerobase.mytable.dto;

import com.zerobase.mytable.domain.Store;
import com.zerobase.mytable.type.Address;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreDto {

    private String storename;

    private String phone;

    private Address address;

    private String description;

    public static StoreDto from(Store store) {
        return StoreDto.builder()
                .storename(store.getStorename())
                .phone(store.getPhone())
                .address(store.getAddress())
                .description(store.getDescription())
                .build();
    }

}
