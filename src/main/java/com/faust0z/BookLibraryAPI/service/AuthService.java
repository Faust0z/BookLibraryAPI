package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.LoginRequestDTO;
import com.faust0z.BookLibraryAPI.dto.LoginResponseDTO;
import com.faust0z.BookLibraryAPI.dto.RegisterRequestDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.exception.EmailAlreadyInUseException;
import com.faust0z.BookLibraryAPI.exception.ResourceUnavailableException;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                       UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail().toLowerCase(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(userDetails);

        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceUnavailableException("User data not found after login."));

        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);

        return new LoginResponseDTO(token, userDTO);
    }

    @CacheEvict(value = "users", key = "'list:all'")
    public UserDTO register(RegisterRequestDTO registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("Email address already in use.");
        }

        UserEntity user = new UserEntity();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        UserEntity savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDTO.class);
    }
}