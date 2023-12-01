package com.zerobase.mytable.domain;

import com.zerobase.mytable.dto.ReviewDto;
import lombok.*;
import org.hibernate.envers.AuditOverride;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AuditOverride(forClass = BaseEntity.class)
public class Review extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Store store;

    private String title;

    private String text;

    public static Review from(ReviewDto request, Customer customer, Store store) {
        return Review.builder()
                .customer(customer)
                .store(store)
                .title(request.getTitle())
                .text(request.getText())
                .build();
    }
}
