package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.AdminUserDTO;
import com.faust0z.BookLibraryAPI.dto.MyUserDetailsDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateUserDTO;
import com.faust0z.BookLibraryAPI.dto.UpdateUserPasswordDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.entity.UserEntity;
import com.faust0z.BookLibraryAPI.exception.IncorrectPasswordException;
import com.faust0z.BookLibraryAPI.exception.ResourceNotFoundException;
import com.faust0z.BookLibraryAPI.exception.SamePasswordException;
import com.faust0z.BookLibraryAPI.mapper.UserMapper;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void getAllUsers_ShouldReturnListWithTwoDtos() {
        UserEntity entity1 = new UserEntity();
        UserEntity entity2 = new UserEntity();
        List<UserEntity> entityList = List.of(entity1, entity2);

        AdminUserDTO dto1 = new AdminUserDTO();
        AdminUserDTO dto2 = new AdminUserDTO();
        List<AdminUserDTO> dtoList = List.of(dto1, dto2);

        when(userRepository.findAll()).thenReturn(entityList);
        when(userMapper.toAdminDtoList(entityList)).thenReturn(dtoList);

        List<AdminUserDTO> result = userService.getAllUsers();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(dto1, dto2);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnDTO() {
        UUID userId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setId(userId);
        AdminUserDTO expectedDto = new AdminUserDTO();
        expectedDto.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(userMapper.toAdminDto(entity)).thenReturn(expectedDto);

        AdminUserDTO result = userService.getUserbyId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldTriggerException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserbyId(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMyDetails_WhenUserExists_ShouldReturnDto() {
        UUID userId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setId(userId);
        MyUserDetailsDTO expectedDto = new MyUserDetailsDTO();
        expectedDto.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(userMapper.toMyDetailsDto(entity)).thenReturn(expectedDto);

        MyUserDetailsDTO result = userService.getMyDetails(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void getMyDetails_WhenUserDeletedButTokenValid_ShouldThrowException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyDetails(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_WhenUserExists_ShouldReturnDto() {
        UUID userId = UUID.randomUUID();
        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(userId);
        UpdateUserDTO updateDto = new UpdateUserDTO();
        updateDto.setName("Tom");
        UserDTO expectedDto = new UserDTO();
        expectedDto.setName("Tom");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(existingEntity)).thenReturn(existingEntity);
        when(userMapper.toDto(existingEntity)).thenReturn(expectedDto);

        UserDTO result = userService.updateUser(userId, updateDto);

        verify(userMapper).updateUserFromDto(updateDto, existingEntity);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(updateDto.getName());
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldTriggerException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(UUID.randomUUID(), new UpdateUserDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUserPassword_WhenUserExistsAndPasswordIsValid_ShouldReturnDTO() {
        UUID userId = UUID.randomUUID();
        String oldPass = "123456";
        String oldPassHash = "hashOf123456";
        String newPass = "987654";
        String newPassHash = "hashOf987654";

        UserEntity entity = new UserEntity();
        entity.setId(userId);
        entity.setPassword(oldPassHash);

        UpdateUserPasswordDTO updateDTO = new UpdateUserPasswordDTO();
        updateDTO.setCurrentPassword(oldPass);
        updateDTO.setNewPassword(newPass);

        MyUserDetailsDTO expectedDto = new MyUserDetailsDTO();
        expectedDto.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches(oldPass, oldPassHash)).thenReturn(true);
        when(passwordEncoder.matches(newPass, oldPassHash)).thenReturn(false);
        when(passwordEncoder.encode(newPass)).thenReturn(newPassHash);
        when(userRepository.save(any(UserEntity.class))).thenReturn(entity);
        when(userMapper.toMyDetailsDto(entity)).thenReturn(expectedDto);

        MyUserDetailsDTO result = userService.updateUserPassword(userId, updateDTO);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode(newPass);
        verify(userRepository).save(entity);
    }

    @Test
    void updateUserPassword_WhenCurrentPasswordIsIncorrect_ShouldTriggerException() {
        UUID userId = UUID.randomUUID();
        String currentPass = "realHash";
        String wrongPass = "wrongHash";

        UserEntity entity = new UserEntity();
        entity.setPassword(currentPass);

        UpdateUserPasswordDTO updateDto = new UpdateUserPasswordDTO();
        updateDto.setCurrentPassword(wrongPass);

        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches(wrongPass, currentPass)).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUserPassword(userId, updateDto))
                .isInstanceOf(IncorrectPasswordException.class);
    }

    @Test
    void updateUserPassword_WhenPasswordIsInvalid_ShouldTriggerException() {
        UUID userId = UUID.randomUUID();
        String oldPass = "123456";
        String oldPassHash = "hashOf123456";

        UserEntity entity = new UserEntity();
        entity.setPassword(oldPassHash);

        UpdateUserPasswordDTO dto = new UpdateUserPasswordDTO();
        dto.setCurrentPassword(oldPass);
        dto.setNewPassword(oldPass);

        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches(oldPass, oldPassHash)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUserPassword(userId, dto))
                .isInstanceOf(SamePasswordException.class);
    }
}