package ru.practicum.shareit.user.service;

import java.util.List;
import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    /**
     * Find user dto by id.
     * @param id User id.
     * @return UserDto.
     */
    UserDto findById(Long id);

    /**
     * Find all users dto.
     * @return List users dto.
     */
    List<UserDto> findAll();

    /**
     * Create user dto.
     * @param userDto Entity.
     * @return UserDto.
     */
    UserDto save(UserDto userDto);

    /**
     * Update user dto by id.
     * @param userDto Entity.
     * @param id User id.
     * @return UserDto.
     */
    UserDto update(UserDto userDto, Long id);

    /**
     * Delete user dto by id.
     * @param id User id.
     */
    void deleteById(Long id);
}
