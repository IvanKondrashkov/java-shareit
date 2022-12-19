package ru.practicum.shareit.user.service;

import java.util.List;
import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    /**
     * Find user by id.
     * @param id User id.
     * @return UserDto.
     */
    UserDto findById(Long id);

    /**
     * Find all users.
     * @return List user dto.
     */
    List<UserDto> findAll();

    /**
     * Create user.
     * @param userDto Entity.
     * @return UserDto.
     */
    UserDto save(UserDto userDto);

    /**
     * Update user by id.
     * @param userDto Entity.
     * @param id User id.
     * @return UserDto.
     */
    UserDto update(UserDto userDto, Long id);

    /**
     * Delete user by id.
     * @param id User id.
     */
    void deleteById(Long id);
}
