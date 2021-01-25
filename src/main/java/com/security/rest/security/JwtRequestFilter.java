package com.security.rest.security;

import com.security.rest.service.JwtUtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilService jwtUtilService;

    private final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // JWT Token is in the form "Bearer token". Remove Bearer word and
        // get  only the Token
        String jwtToken = jwtUtilService.extractJwtFromRequest(request);

        // Don't need to get the password or give it our own user obj because at this point
        // We would have already passed our Spring config and just need to pass the token

        // StringUtils.hasText checks to make sure token is not null and length is greater than 0
        // Using service method to see if it is a valid token
        if (StringUtils.hasText(jwtToken) && jwtUtilService.validateToken(jwtToken)) {
            UserDetails userDetails = new User(jwtUtilService.getUsernameFromToken(jwtToken), "",
                    jwtUtilService.getRolesFromToken(jwtToken));

            logger.info("Reached to here");
            // These steps by pass the need for AuthManager because it produces a full Authentication object
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            // After setting the Authentication in the context, we specify
            // that the current user is authenticated. So it passes the
            // Spring Security Configurations successfully.
            logger.info("UP Auth Token: " + usernamePasswordAuthenticationToken);
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        } else {
            logger.info("Cannot set the Security Context");

        }

        filterChain.doFilter(request, response);

    }

}
