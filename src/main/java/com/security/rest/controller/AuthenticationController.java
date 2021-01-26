package com.security.rest.controller;

import com.security.rest.email.EmailConfiguration;
import com.security.rest.model.AuthenticationRequest;
import com.security.rest.model.AuthenticationResponse;
import com.security.rest.model.NewUserRequest;
import com.security.rest.model.User;
import com.security.rest.repository.UserRepository;
import com.security.rest.security.JwtRequestFilter;
import com.security.rest.service.JwtUtilService;
import com.security.rest.service.MyUserDetailsService;
import com.security.rest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000", "https://react-security-product-app.herokuapp.com/"})
@RestController
public class AuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private JwtUtilService jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailConfiguration emailCfg;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<Map<String, String>> createAuthenticationToken(@RequestBody(required = false) AuthenticationRequest authenticationRequest)
            throws Exception {

        logger.info("Made it here");

        logger.info(String.valueOf(authenticationRequest));

        try {
            // Uses the AuthenticationManager bean to create instance
            // The instance is just an interface so the manager uses the AuthManager from
            // Our AuthManagerBuilder configure method to produce our strategy
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        // After user is authenticated we generate the token
        final UserDetails userDetails = myUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        User loggedUser = userRepository.findByUsername(authenticationRequest.getUsername());

        String firstName = loggedUser.getFirstName();

        String authority = loggedUser.getRole();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("firstName", firstName);
        response.put("credentials", authority);
//        return ResponseEntity.ok(Arrays.asList(new AuthenticationResponse(token), authenticationRequest.getUsername()));

        return ResponseEntity.ok(response);

    }

    @PostMapping(value = "/authenticate/new")
    public User createNewUser(@RequestBody NewUserRequest newUserRequest){

        User newUser = new User(newUserRequest.getUsername(), newUserRequest.getFirstName(),
                newUserRequest.getLastName(), "USER", newUserRequest.getEmail(), newUserRequest.getPassword());

        // Create Mail Sender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailCfg.getHost());
        mailSender.setPort(emailCfg.getPort());
        mailSender.setUsername(emailCfg.getUsername());
        mailSender.setPassword(emailCfg.getPassword());

        // Create Email Instance
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("security-app@welcome.com");
        mailMessage.setTo(newUserRequest.getEmail());
        mailMessage.setSubject("Thank You For Signing Up " + newUserRequest.getFirstName());
        mailMessage.setText("Hello, welcome to securityapp.com where we handle all REST security needs!");

        // Send Mail
        mailSender.send(mailMessage);

        return userService.saveNewUser(newUser);

    }

    @PostMapping(value = "/authenticate/new/admin")
    public User createNewAdmin(@RequestBody NewUserRequest newUserRequest){

        User newAdmin = new User(newUserRequest.getUsername(), newUserRequest.getFirstName(),
                newUserRequest.getLastName(), "ADMIN", newUserRequest.getEmail(), newUserRequest.getPassword());

        return userService.saveNewUser(newAdmin);

    }

}
