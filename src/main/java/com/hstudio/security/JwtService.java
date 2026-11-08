package com.hstudio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/** Cr et v les jetons JWT des utilisateurs. */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 heures par défaut
    private long jwtExpiration;

    /** Lit dans le jeton identifiant de connexion de utilisateur. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extrait du jeton la donnée demandée, après vérification de sa signature. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Cr un jeton signe avec utilisateur et sa date expiration. */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /** V que le jeton correspond a utilisateur et ne a pas expire. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /** Vérifie si la date d’expiration du jeton est dépassée. */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** Recupere les informations utiles a op extractExpiration. */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** V la signature puis lit les informations contenues dans le jeton. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Transforme la cle configur en cle cryptographique de signature. */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}