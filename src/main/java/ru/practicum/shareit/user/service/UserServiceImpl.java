package ru.practicum.shareit.user.service;

import java.util.List;
import java.util.stream.Collectors;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dao.UserStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto findById(Long id) {
        final User user = userStorage.findById(id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto save(UserDto userDto) {
        final User user = UserMapper.toUser(userDto);
        userStorage.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(UserDto userDto, Long id) {
        final User user = UserMapper.toUser(userDto);
        userStorage.update(user, id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteById(Long id) {
        userStorage.deleteById(id);
    }
}
