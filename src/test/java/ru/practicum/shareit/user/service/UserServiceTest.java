package ru.practicum.shareit.user.service;

import java.util.List;
import java.util.Optional;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.repo.UserRepository;
import javax.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private User user;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    @Captor
    private ArgumentCaptor<User> captor;

    @BeforeEach
    void init() {
        user = new User(1L, "Djon", "djon@mail.ru");
    }

    @AfterEach
    void tearDown() {
        user = null;
    }

    @Test
    void findById() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto dto = userService.findById(user.getId());

        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getEmail(), user.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.findById(userId);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void findAll() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> users = userService.findAll();

        assertEquals(1, users.size());

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    void save() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);

        UserDto dto = UserMapper.toUserDto(user);
        UserDto savedDto = userService.save(dto);

        assertEquals(savedDto.getId(), dto.getId());
        assertEquals(savedDto.getName(), dto.getName());
        assertEquals(savedDto.getEmail(), dto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void update() {
        User newUser = new User(user.getId(), "Mike", "mike@mail.ru");
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto dto = UserMapper.toUserDto(newUser);
        userService.update(dto, newUser.getId());

        Mockito.verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertEquals(savedUser.getId(), newUser.getId());
        assertEquals(savedUser.getName(), newUser.getName());
        assertEquals(savedUser.getEmail(), newUser.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void updateByNotValidId(Long userId) {
        User newUser = new User(userId, "Mike", "mike@mail.ru");
        UserDto dto = UserMapper.toUserDto(newUser);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.update(dto, userId);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteById() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteById(user.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteByNotValidId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteById(userId);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }
}
