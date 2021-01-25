package com.security.rest.security;

import com.security.rest.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    MyUserDetailsService myUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    @Bean
    public BCryptPasswordEncoder createBCryptBean() {
        return new BCryptPasswordEncoder();
    }

    // Rather than providing a bean with an AuthenticationProvider object and passing our UserDetails Service
    // In to that, we our using the AuthenticationManagerBuilder object to create a DaoAuthProv for us using the
    // userDetailsService method which just needs our UserDetails Service and tell it how we want it encoded
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(myUserDetailsService).passwordEncoder(createBCryptBean());

    }

    // Authentication ---> Identification of User
    // Authorization ---> Determining Access of Identified User

    // Set up security context configuration so that we first check for a token
    // ---> If a token is found it is parsed and if verified User is authenticated in Security Context
    // ---> If no token is found user goes through Spring authentication and authenticated users are given token
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Not about CSRF
//        http
                http.cors().and().csrf().disable()
                .authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("USER","ADMIN")
//                .antMatchers("/feedback").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.POST, "/authenticate/**").permitAll()
                // ---> Swagger Path is "/swagger-ui/index.html"
                .antMatchers("/swagger-ui/**", "/v2/api-docs",
                        "/swagger-resources/**",
                        "/swagger-ui.html**",
                        "/webjars/**").permitAll().anyRequest().authenticated()
                //if any exception occurs call this
                .and().exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler).and().
                // make sure we use stateless session; session won't be used to
                // store user's state.
                        sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

                http.cors();

// 		Add a filter to validate the tokens with every request
        http.addFilterBefore(jwtRequestFilter,
                UsernamePasswordAuthenticationFilter.class);
    }
}
