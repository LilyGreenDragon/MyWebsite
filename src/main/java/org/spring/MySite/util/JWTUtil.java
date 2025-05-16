package org.spring.MySite.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;
import java.util.Date;

public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(String username) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        return JWT.create()
                .withSubject("User")
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withIssuer("egorchik")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public  String validateToken(String token)throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User")
                .withIssuer("egorchik")
                .build();

        DecodedJWT jwt =  verifier.verify(token);
       return jwt.getClaim("username").asString();

    }
}
