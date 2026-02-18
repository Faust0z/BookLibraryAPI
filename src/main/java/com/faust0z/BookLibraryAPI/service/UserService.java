package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.AdminUserDTO;
import com.faust0z.BookLibraryAPI.dto.MyUserDetailsDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateUserDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateUserPasswordDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.exception.InvalidPasswordException;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.exception.SamePasswordException;
import com.faust0z.BookLibraryAPI.mapper.UserMapper;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Cacheable(value = "users", key = "'list:all'")
    public List<AdminUserDTO> getAllUsers() {
        return userMapper.toAdminDtoList(userRepository.findAll());
    }

    @Cacheable(value = "users", key = "'detail:' + #userId")
    public AdminUserDTO getUserbyId(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toAdminDto(user);
    }

    @Cacheable(value = "user_details", key = "#userId")
    public MyUserDetailsDTO getMyDetails(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found (Might have been deleted)"));

        return userMapper.toMyDetailsDto(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "'detail:' + #userId"),
            @CacheEvict(value = "users", key = "'list:all'")
    })
    @Transactional
    public UserDTO updateUser(UUID userId, UpdateUserDTO dto) {
        UserEntity existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userMapper.updateUserFromDto(dto, existingUser);

        UserEntity updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public MyUserDetailsDTO updateUserPassword(UUID userId, UpdateUserPasswordDTO dto) {
        UserEntity existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), existingUser.getPassword())) {
            throw new InvalidPasswordException("Provided current password is incorrect");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), existingUser.getPassword())) {
            throw new SamePasswordException("New password cannot be the same as the old password");
        }

        existingUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        UserEntity updatedUser = userRepository.save(existingUser);
        return userMapper.toMyDetailsDto(updatedUser);
    }
}
