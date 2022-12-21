package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repo.UserRepository;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;

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
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto save(UserDto userDto) {
        final User user = UserMapper.toUser(userDto);
        try {
            final User userWrap = userRepository.save(user);
            return UserMapper.toUserDto(userWrap);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException(String.format("User with the email=%s already exists", user.getEmail()));
        }
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
        final User user = UserMapper.toUser(userDto);
        final User userWrap = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", id))
        );
        Optional.ofNullable(user.getName()).ifPresent(opt -> userWrap.setName(user.getName()));
        Optional.ofNullable(user.getEmail()).ifPresent(opt -> userWrap.setEmail(user.getEmail()));
        try {
            userRepository.save(userWrap);
            return UserMapper.toUserDto(userWrap);
        } catch (DataIntegrityViolationException e) {
            throw new EntityExistsException(String.format("User with the email=%s already exists", user.getEmail()));
        }
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
