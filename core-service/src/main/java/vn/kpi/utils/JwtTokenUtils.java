package vn.kpi.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.kpi.models.UserTokenDto;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenUtils {
    // TODO: Production thi chinh lai
    @Value("${service.token.secretKey}")
    private String tokenSecret;
    @Value("${service.accessToken.timeout.seconds}")
    private Integer timeOutAccessToken;
    @Value("${service.refreshToken.timeout.seconds}")
    private Integer timeOutRefreshToken;

    public UserTokenDto validateToken(String jwtString) {
        if (jwtString == null) {
            return null;
        }
        try {
            return getUerNameFromToken(jwtString, null);
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("JWT signature invalid: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformed: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("JWT validation error: {}", ex.getMessage(), ex);
        }
        return null;
    }

    public UserTokenDto getUerNameFromToken(String token, String secretKey) throws ClassNotFoundException {
        final Claims claims = getAllClaimsFromToken(token, secretKey);
        String userInfo = (String) claims.get("userInfo");
        String className = (String) claims.get("className");
        return (UserTokenDto) Utils.fromJson(userInfo, Class.forName(className));

    }

    private Key getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserTokenDto userTokenDto, boolean isAccessToken) {
        Key signingKey = getSigningKey(tokenSecret);

        int expirationSeconds = isAccessToken ? timeOutAccessToken : timeOutRefreshToken;

        JwtBuilder builder = Jwts.builder()
                .setId(userTokenDto.getId())
                .setIssuer(userTokenDto.getIssuer())
                .setSubject(userTokenDto.getSubject())
                .setAudience(userTokenDto.getAudience())
                .setExpiration(DateUtils.addMilliseconds(new Date(), expirationSeconds * 1000))
                .setNotBefore(new Date())
                .setIssuedAt(new Date())
                .claim("userInfo", Utils.toJson(userTokenDto))
                .claim("className", userTokenDto.getClass().getName())
                .setHeaderParam("type", "JWS")
                .signWith(signingKey, SignatureAlgorithm.HS512);

        if (!isAccessToken) {
            builder.claim("isRefreshToken", "true");
        }
        return builder.compact();
    }

    private Claims getAllClaimsFromToken(String token, String secretKey) {
        String key = (secretKey != null) ? secretKey : tokenSecret;
        Key signingKey = getSigningKey(key);

        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
