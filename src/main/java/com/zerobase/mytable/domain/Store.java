package com.zerobase.mytable.domain;

import com.zerobase.mytable.dto.StoreRegisterDto;
import com.zerobase.mytable.type.Address;
import lombok.*;
import org.hibernate.envers.AuditOverride;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
public class Store extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String storename;

    @Column
    private String phone;

    @Column(nullable = false)
    @Embedded
    private Address address;

    // 상점에 대한 설명 입력
    @Column(nullable = false)
    private String description;

    @ManyToOne
    private Partner partner;

    @OneToMany(mappedBy = "store", cascade = CascadeType.PERSIST)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.PERSIST)
    private List<Review> reviews = new ArrayList<>();

    public static Store from(StoreRegisterDto.Request request){
        return Store.builder()
                .storename(request.getStorename())
                .phone(request.getPhone())
                .address(Address.builder()
                        .sido(request.getSido())
                        .sigungu(request.getSigungu())
                        .roadname(request.getRoadname())
                        .detailAddress(request.getDetailAddress())
                        .build())
                .description(request.getDescription())
                .build();
    }
}
