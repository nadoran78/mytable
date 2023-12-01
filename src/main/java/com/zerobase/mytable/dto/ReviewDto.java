package com.zerobase.mytable.dto;

import com.zerobase.mytable.domain.Review;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDto {

    @Null
    private Long id;

    @Null
    private String name;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    private String storename;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Pattern(regexp = "[\\S+\\s*]{2,30}", message = "2자 이상, 30자 이하 입력하셔야 합니다.")
    private String title;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Pattern(regexp = "[\\S+\\s*]{5,300}", message = "5자 이상, 300자 이하 입력하셔야 합니다.")
    private String text;

    @Null
    private LocalDateTime createdAt;

    public static ReviewDto from(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .name(nameMasking(review.getCustomer().getName()))
                .storename(review.getStore().getStorename())
                .title(review.getTitle())
                .text(review.getText())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private static String nameMasking(String name) {
        String koreanNameMasking = "(?<=.{1})(?<masking>.*)(?=.$)";
        String anyNameMasking = "(?<=.{1})(?<masking>\\w*)(?=\\s)";
        String maskingStar = "*";

        if (name.length() < 3) {
            return name.substring(0, name.length() - 1) + maskingStar;
        }

        if (name.charAt(0) >= 65 && name.charAt(0) <= 122) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    anyNameMasking);
            Matcher m = pattern.matcher(name);

            if (m.find()) {
                return name.replaceFirst(anyNameMasking,
                        maskingStar.repeat(m.group().length()));
            }
        }
        return name.replaceFirst(koreanNameMasking,
                maskingStar.repeat(name.length() - 2));
    }


}
