package ru.practicum.shareit.user.dao;

import java.util.*;
import ru.practicum.shareit.user.model.User;
import java.util.concurrent.atomic.AtomicLong;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.FieldWithoutException;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import org.springframework.stereotype.Repository;

@Repository
public class UserStorageImpl implements UserStorage {
    private AtomicLong idCurrent = new AtomicLong();
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    private Long getIdCurrent() {
        return idCurrent.incrementAndGet();
    }

    @Override
    public User findById(Long id) {
        if (!users.containsKey(id)) {
            throw new EntityNotFoundException(String.format("User with id=%d not found!", id));
        }
        return users.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User save(User user) {
        if (emails.contains(user.getEmail())) {
           throw new EmailAlreadyExistsException(String.format("User with the email=%s already exists"
                   , user.getEmail())
           );
        }
        if (user.getEmail() == null) {
            throw new FieldWithoutException(String.format("User without email=%s"
                    , user.getEmail())
            );
        }
        user.setId(getIdCurrent());
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public User update(User user, Long id) {
        if (emails.contains(user.getEmail())) {
            throw new EmailAlreadyExistsException(String.format("User with the email=%s already exists"
                    , user.getEmail())
            );
        }
        if (!users.containsKey(id)) {
            throw new EntityNotFoundException(String.format("User with id=%d not found!"
                    , id)
            );
        }
        final User oldUser = users.get(id);
        user.setId(id);
        user.setName(user.getName() != null ? user.getName() : oldUser.getName());
        user.setEmail(user.getEmail() != null ? user.getEmail() : oldUser.getEmail());
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        emails.remove(oldUser.getEmail());
        return user;
    }

    @Override
    public void deleteById(Long id) {
        if (!users.containsKey(id)) {
            throw new EntityNotFoundException(String.format("User with id=%d not found!", id));
        }
        final User user = users.get(id);
        users.remove(id);
        emails.remove(user.getEmail());
    }
}
