package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.BookerDto;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private static final User USER = new User(1L, "Nikolas", "nik@mail.ru");
    private static final UserDto DTO = UserMapper.toUserDto(USER);

    @Test
    void toUserDto() {
        UserDto dto = UserMapper.toUserDto(USER);

        assertEquals(dto.getId(), USER.getId());
        assertEquals(dto.getName(), USER.getName());
        assertEquals(dto.getEmail(), USER.getEmail());
    }

    @Test
    void toBookerDto() {
        BookerDto dto = UserMapper.toBookerDto(USER);

        assertEquals(dto.getId(), USER.getId());
    }

    @Test
    void toUser() {
        User user = UserMapper.toUser(DTO);

        assertEquals(user.getId(), DTO.getId());
        assertEquals(user.getName(), DTO.getName());
        assertEquals(user.getEmail(), DTO.getEmail());
    }
}
