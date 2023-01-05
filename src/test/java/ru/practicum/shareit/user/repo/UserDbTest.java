package ru.practicum.shareit.user.repo;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.TypedQuery;
import javax.persistence.EntityManager;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbTest {
    private User user;
    private final EntityManager em;
    private final UserService userService;

    @BeforeEach
    void init() {
        user = new User(null, "Djon", "djon@mail.ru");
    }

    @AfterEach
    void tearDown() {
        user = null;
    }

    @Test
    void findById() {
        UserDto dto = makeUser(user);
        TypedQuery<Long> query = em.createQuery("select u.id from User as u where u.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        dto = userService.findById(id);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findAll() {
        makeUsers();
        TypedQuery<User> query = em.createQuery("select u from User as u", User.class);
        List<User> result = query.getResultList();
        List<UserDto> users = userService.findAll();

        assertThat(result.size()).isEqualTo(users.size());
    }

    @Test
    void save() {
        UserDto dto = makeUser(user);
        TypedQuery<User> query = em.createQuery("select u from User as u where u.email = :email", User.class);
        user = query
                .setParameter("email", dto.getEmail())
                .getSingleResult();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo(dto.getName());
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void saveByEmailExists() {
        UserDto dto = makeUser(user);
        EntityExistsException exception = assertThrows(EntityExistsException.class, () -> {
            makeUser(user);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with the email=%s already exists", user.getEmail());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void update() {
        UserDto dto = makeUser(user);
        TypedQuery<User> query = em.createQuery("select u from User as u where u.id = :id", User.class);
        user = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        User newUser = new User(user.getId(), "Mike", "mike@mail.ru");
        dto = UserMapper.toUserDto(newUser);
        dto = userService.update(dto, dto.getId());

        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getName()).isEqualTo(dto.getName());
        assertThat(newUser.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void deleteById() {
        UserDto dto = makeUser(user);
        TypedQuery<Long> query = em.createQuery("select u.id from User as u where u.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        userService.deleteById(id);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.findById(id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);
    }

    private UserDto makeUser(User user) {
        UserDto dto = UserMapper.toUserDto(user);
        return userService.save(dto);
    }

    private void makeUsers() {
        userService.save(new UserDto(null, "Djon", "djon@mail.ru"));
        userService.save(new UserDto(null, "Bob", "bob@mail.ru"));
        userService.save(new UserDto(null, "Mike", "mike@mail.ru"));
    }
}
