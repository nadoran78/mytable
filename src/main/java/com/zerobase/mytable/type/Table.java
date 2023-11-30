package com.zerobase.mytable.type;

import lombok.*;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Table {
    private Integer tableVolume;
    private Integer tableAmount;
}
