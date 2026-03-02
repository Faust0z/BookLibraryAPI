package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // "username" = user email.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading UserDetails for username (email): {}", username);
        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.debug("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }
}
