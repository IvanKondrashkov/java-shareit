package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repo.UserRepository;
import javax.persistence.EntityNotFoundException;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto findById(Long id) {
        final User userWrap = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", id))
        );
        return UserMapper.toUserDto(userWrap);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public UserDto save(UserDto userDto) {
        final User user = UserMapper.toUser(userDto);
        final User userWrap = userRepository.save(user);
        return UserMapper.toUserDto(userWrap);
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
        final User user = UserMapper.toUser(userDto);
        final User userWrap = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", id))
        );
        Optional.ofNullable(user.getName()).ifPresent(it -> {
            if (!user.getName().isBlank()) userWrap.setName(user.getName());
        });
        Optional.ofNullable(user.getEmail()).ifPresent(it -> {
            if (!user.getEmail().isBlank()) userWrap.setEmail(user.getEmail());
        });
        return UserMapper.toUserDto(userWrap);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        final User userWrap = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", id))
        );
        userRepository.deleteById(userWrap.getId());
    }
}
