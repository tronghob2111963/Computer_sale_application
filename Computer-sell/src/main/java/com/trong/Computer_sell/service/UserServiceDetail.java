package com.trong.Computer_sell.service;

import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public record UserServiceDetail(UserRepository userRepository) {

    public UserDetailsService UserServiceDetail() {
        return userRepository::findByUsername;
    }
}