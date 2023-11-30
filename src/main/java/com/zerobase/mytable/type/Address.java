package com.zerobase.mytable.type;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String sido;
    private String sigungu;
    private String roadname;
    private String detailAddress;
}
