package com.zerobase.mytable.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zerobase.mytable.dto.SignUpDto;
import com.zerobase.mytable.type.UserType;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
public class Customer extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // id는 인덱스로 사용되기 때문에 고유 식별자 사용
    @Column(unique = true, nullable = false)
    private String uid;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDate birth;

    // 사용권한 분류, Collection 형태 저장할 때 쓰는 어노테이션
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> roles;

    // 계정이 가지고 있는 권한 목록을 리턴
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public String getUsername() {
        return this.uid;
    }

    // 계정 만료 여부 리턴, true면 만료 안 됨
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겨있는지 여부 리턴, true면 안 잠김
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 리턴, true면 만료 안 됨
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 리턴, true면 활성화
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isEnabled() {
        return true;
    }

    public static Customer from(SignUpDto.Request request){
        return Customer.builder()
                .uid(UUID.randomUUID().toString().replace("-", ""))
                .email(request.getEmail())
                .name(request.getName())
                .password(request.getPassword())
                .phone(request.getPhone())
                .birth(request.getBirth())
                .roles(Collections.singletonList(UserType.ROLE_CUSTOMER.toString()))
                .build();
    }
}
