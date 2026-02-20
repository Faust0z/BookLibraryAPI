package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.LoginRequestDTO;
import com.faust0z.BookLibraryAPI.dto.LoginResponseDTO;
import com.faust0z.BookLibraryAPI.dto.RegisterRequestDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.exception.EmailAlreadyInUseException;
import com.faust0z.BookLibraryAPI.exception.ResourceUnavailableException;
import com.faust0z.BookLibraryAPI.mapper.UserMapper;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnTokenAndUserDto() {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("TEST@Example.com");
        loginRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        UserEntity userEntity = new UserEntity();
        UserDTO userDTO = new UserDTO();
        String mockToken = "mocked-jwt-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn(mockToken);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(userDTO);

        LoginResponseDTO response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(mockToken);
        assertThat(response.getUser()).isEqualTo(userDTO);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_WhenUserMissingInDbAfterAuth_ShouldThrowResourceUnavailableException() {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceUnavailableException.class)
                .hasMessageContaining("User data not found after login");
    }

    @Test
    void register_WhenEmailIsNew_ShouldEncodePasswordAndSaveUser() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("NEW@Example.com");
        registerRequest.setPassword("rawPassword");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("NEW@Example.com");
        UserEntity savedEntity = new UserEntity();
        UserDTO expectedDto = new UserDTO();

        when(userRepository.findByEmail("NEW@Example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(registerRequest)).thenReturn(userEntity);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(expectedDto);

        UserDTO result = authService.register(registerRequest);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(userEntity.getEmail()).isEqualTo("new@example.com");
        assertThat(userEntity.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).save(userEntity);
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowEmailAlreadyInUseException() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new UserEntity()));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyInUseException.class)
                .hasMessageContaining("Email address already in use");
    }
}