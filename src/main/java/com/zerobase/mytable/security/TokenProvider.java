package com.zerobase.mytable.security;

import com.zerobase.mytable.exception.CustomException;
import com.zerobase.mytable.type.ErrorCode;
import com.zerobase.mytable.service.PartnerService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

    private final PartnerService partnerService;

    @Value("${springboot.jwt.secret}")
    private String secretKey;

    private final long tokenValidMillisecond = 1000L * 60 * 60 * 24;

    // secretKey를 Base64 형식으로 인코딩(빈으로 등록될 때 실행되는 메서드)
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder()
                .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // uid와 권한을 입력하여 토큰 생성
    public String createToken(String uid, List<String> roles){
        Claims claims = Jwts.claims().setSubject(uid);
        claims.put("roles", roles);

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidMillisecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 토큰 인증 정보 조회 및 Authentication 생성
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = partnerService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "",
                userDetails.getAuthorities());
    }

    // 토큰으로부터 uid 추출
    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().getSubject();
    }

    // http 헤더에서 token 값 추출
    public String resolveToken(HttpServletRequest request){
        return request.getHeader("X-AUTH_TOKEN");
    }

    // 토큰 유효성 체크
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey)
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
