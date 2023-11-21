package com.zerobase.mytable.domain;

import com.zerobase.mytable.dto.StoreRegisterDto;
import lombok.*;
import org.hibernate.envers.AuditOverride;

import javax.persistence.*;
import java.util.UUID;

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

    // 점포 입력 받을 때 주소 입력(추후 통계 처리할 경우를 위해 시도/ 시군구/ 도로명 / 상세주소로 나눠서 받음)
    @Column(nullable = false)
    private String sido;

    @Column(nullable = false)
    private String sigungu;

    @Column(nullable = false)
    private String roadname;

    @Column(nullable = false)
    private String detailAddress;

    // 상점에 대한 설명 입력
    @Column(nullable = false)
    private String description;

    @ManyToOne
    private Partner partner;

    public static Store from(StoreRegisterDto.Request request){
        return Store.builder()
                .storename(request.getStorename())
                .phone(request.getPhone())
                .sido(request.getSido())
                .sigungu(request.getSigungu())
                .roadname(request.getRoadname())
                .detailAddress(request.getDetailAddress())
                .description(request.getDescription())
                .build();
    }
}
