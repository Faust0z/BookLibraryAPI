package com.faust0z.BookLibraryAPI.service;

import com.faust0z.BookLibraryAPI.dto.CreateUserDTO;
import com.faust0z.BookLibraryAPI.dto.UserDTO;
import com.faust0z.BookLibraryAPI.model.User;
import com.faust0z.BookLibraryAPI.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gets all users from the database.
     *
     * @return A list of UserDTOs.
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new Users.
     *
     * @param userDTO The DTO containing the new Users's data.
     * @return The created Users's data as a DTO.
     */
    public UserDTO createUser(CreateUserDTO userDTO) {
        User User = new User();
        User.setName(userDTO.getName());
        User.setEmail(userDTO.getEmail());

        User savedUser = userRepository.save(User);
        return convertToDto(savedUser);
    }

    /**
     * Helper method to convert a Users entity to a UserDTO.
     */
    private UserDTO convertToDto(User User) {
        UserDTO dto = new UserDTO();
        dto.setId(User.getId());
        dto.setName(User.getName());
        dto.setEmail(User.getEmail());
        return dto;
    }
}
