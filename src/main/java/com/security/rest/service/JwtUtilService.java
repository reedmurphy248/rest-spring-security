package com.security.rest.service;

import com.security.rest.model.User;
import com.security.rest.repository.UserRepository;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class JwtUtilService {

    @Autowired
    UserRepository userRepository;

    private String secret;
    private int jwtExpirationInMs;

    private Logger logger = LoggerFactory.getLogger(JwtUtilService.class);

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Value("${jwt.jwtExpirationInMs}")
    public void setJwtExpirationInMs(int jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    // generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
        if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            claims.put("isAdmin", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            claims.put("isUser", true);
        }
        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)).signWith(SignatureAlgorithm.HS512, secret).compact();

    }

    // The claims object is just a JWT specific interface implementing a Map<String, Object>
    // Has the following keys :

    //    String ISSUER = "iss";
    //    String SUBJECT = "sub";
    //    String AUDIENCE = "aud";
    //    String EXPIRATION = "exp";
    //    String NOT_BEFORE = "nbf";
    //    String ISSUED_AT = "iat";
    //    String ID = "jti";

    // In order to validate the token on the req header we need to use it as a parameter
    // Check to make sure
    public boolean validateToken(String authToken) {
        try {
            // Jwt token has not been tampered with
            // Essentially we are setting the secret key with which if the token has not be tampered with
            // We would be able to parse the authToken
            // If not one of the four below Exceptions will be thrown
            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        }
    }


    // Returning a full Claims object from parsing the token and returning the subject of the token body
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

        logger.info(claims.getSubject());
        return claims.getSubject();
    }

    // Do some logging and return to this

    // Parsing the token to retrieve the roles
    public List<SimpleGrantedAuthority> getRolesFromToken(String authToken) {
        List<SimpleGrantedAuthority> roles = null;
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken).getBody();
        logger.info(String.valueOf(claims));
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Boolean isUser = claims.get("isUser", Boolean.class);
        if (isAdmin != null && isAdmin == true) {
            roles = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (isUser != null && isUser == true) {
            roles = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles;
    }

    // Is returning the request off of an Authorization header w/o "Bearer " and just the jwt
    public String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public User getLoggedInUser(HttpServletRequest request) {

        String token = extractJwtFromRequest(request);
        String username = getUsernameFromToken(token);

        return userRepository.findByUsername(username);

    }

}
