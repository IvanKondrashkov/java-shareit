package ru.practicum.shareit.user.dao;

import java.util.List;
import ru.practicum.shareit.user.model.User;

public interface UserStorage {
    /**
     * Find user by id.
     * @param id User id.
     * @return User.
     */
    User findById(Long id);

    /**
     * Find all users.
     * @return List users.
     */
    List<User> findAll();

    /**
     * Create user.
     * @param user Entity.
     * @return User.
     */
    User save(User user);

    /**
     * Update user by id.
     * @param user Entity.
     * @param id User id.
     * @return User.
     */
    User update(User user, Long id);

    /**
     * Delete user by id.
     * @param id User id.
     */
    void deleteById(Long id);
}
