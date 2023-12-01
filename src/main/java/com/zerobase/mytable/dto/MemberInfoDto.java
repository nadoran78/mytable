package com.zerobase.mytable.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.mytable.domain.Customer;
import com.zerobase.mytable.domain.Partner;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfoDto {

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Email(message = "이메일 주소가 유효하지 않습니다.")
    private String email;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z-_]{2,10}$",
            message = "이름은 숫자, 특수문자를 제외한 2~10자리여야 합니다.")
    private String name;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}",
            message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Pattern(regexp = "^01([0|1|6|7|8|9]?)-?([0-9]{3,4})-?([0-9]{4})$",
            message = "전화번호가 유효하지 않습니다.")
    private String phone;

    @NotNull(message = "반드시 값이 있어야 합니다.")
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd",
            timezone = "Asia/Seoul")
    private LocalDate birth;


    public static MemberInfoDto fromPartner(Partner partner) {
        String maskingPassword = "**********";
        return MemberInfoDto.builder()
                .email(partner.getEmail())
                .name(partner.getName())
                .password(maskingPassword)
                .phone(partner.getPhone())
                .birth(partner.getBirth())
                .build();
    }

    public static MemberInfoDto fromCustomer(Customer customer) {
        String maskingPassword = "**********";
        return MemberInfoDto.builder()
                .email(customer.getEmail())
                .name(customer.getName())
                .password(maskingPassword)
                .phone(customer.getPhone())
                .birth(customer.getBirth())
                .build();
    }
}
