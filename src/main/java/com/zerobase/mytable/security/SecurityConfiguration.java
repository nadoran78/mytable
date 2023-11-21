package com.zerobase.mytable.security;

import com.zerobase.mytable.exception.CustomAccessDeniedHandler;
import com.zerobase.mytable.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration {

    private final TokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // UI를 사용하는 것을 기본값으로 가진 시큐리티 설정을 비활성화
        httpSecurity.httpBasic().disable()
                // csrf 보안 비활성화
                .csrf().disable()
                // 세션 관리 방식 설정, 세션 사용하지 않을 것이기 때문에 STATELESS 선택
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                    // 애플리케이션에 들어오는 요청에 대한 사용 권한을 체크, url마다 설정 가능
                    .authorizeRequests()
                    .anyRequest().permitAll()
                .and()
                    // 권한 확인 과정에서 통과하지 못하는 예외가 발생할 경우 예외 전달
                    .exceptionHandling()
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                    // 인증 과정에서 예외가 발생할 경우 예외를 전달
                    .exceptionHandling()
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())

                .and()
                    // UsernamePasswordAuthenticationFilter 앞에 JwtAuthenticationFilter 적용
                    .addFilterBefore(new JwtAuthenticationFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // 시큐리티 필터 미적용 대상 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (webSecurity) -> webSecurity.ignoring()
                        .antMatchers("/v2/api-docs", "**swagger**");
    }

    // password 암호화 설정
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
