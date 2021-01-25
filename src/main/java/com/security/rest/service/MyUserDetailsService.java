package com.security.rest.service;

import com.security.rest.model.CurrentUser;
import com.security.rest.model.User;
import com.security.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User foundUser = userRepository.findByUsername(username);

        if (foundUser == null) throw new UsernameNotFoundException("User does not exist");

        return new CurrentUser(foundUser);
    }

}
