package ru.practicum.shareit.request.repo;

import java.util.List;
import java.time.LocalDateTime;
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
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import javax.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDbTest {
    private User requestor;
    private ItemRequest request;
    private final EntityManager em;
    private final UserService userService;
    private final ItemRequestService requestService;

    @BeforeEach
    void init() {
        requestor = new User(null, "Djon", "djon@mail.ru");
        request = new ItemRequest(null, "Good item!", LocalDateTime.now(), requestor);
    }

    @AfterEach
    void tearDown() {
        requestor = null;
        request = null;
    }

    @Test
    void findById() {
        ItemRequestDto dto = makeItemRequest(request);
        TypedQuery<Long> query = em.createQuery("select r.id from ItemRequest as r where r.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        dto = requestService.findById(requestor.getId(), id);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getCreated()).isBefore(LocalDateTime.now());
        assertThat(dto.getItems().size()).isEqualTo(0);
    }

    @Test
    void findAll() {
        makeItemRequests();
        TypedQuery<ItemRequest> query = em.createQuery("select r from ItemRequest as r where r.requestor.id = :id",
                ItemRequest.class);
        List<ItemRequest> result = query
                .setParameter("id", requestor.getId())
                .getResultList();

        List<ItemRequestDto> requests = requestService.findAll(requestor.getId());

        assertThat(result.size()).isEqualTo(requests.size());
    }

    @Test
    void findByPage() {
        makeItemRequests();
        TypedQuery<ItemRequest> query = em.createQuery("select r from ItemRequest as r where not r.requestor.id = :id" +
                        " order by r.created desc", ItemRequest.class);
        List<ItemRequest> result = query
                .setParameter("id", requestor.getId())
                .getResultList();

        List<ItemRequestDto> requests = requestService.findByPage(requestor.getId(), 0, 10);

        assertThat(result.size()).isEqualTo(requests.size());
    }

    @Test
    void save() {
        ItemRequestDto dto = makeItemRequest(request);
        TypedQuery<ItemRequest> query = em.createQuery("select r from ItemRequest as r where r.requestor.email = :email", ItemRequest.class);
        request = query
                .setParameter("email", requestor.getEmail())
                .getSingleResult();

        assertThat(request.getId()).isNotNull();
        assertThat(request.getDescription()).isEqualTo(dto.getDescription());
        assertThat(request.getCreated()).isBefore(LocalDateTime.now());
        assertThat(request.getRequestor().getId()).isNotNull();
    }

    @Test
    void deleteById() {
        ItemRequestDto dto = makeItemRequest(request);
        TypedQuery<Long> query = em.createQuery("select r.id from ItemRequest as r where r.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        requestService.deleteById(requestor.getId(), id);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.findById(requestor.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);
    }

    private ItemRequestDto makeItemRequest(ItemRequest request) {
        UserDto userDto = UserMapper.toUserDto(requestor);
        userDto = userService.save(userDto);
        requestor.setId(userDto.getId());

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        return requestService.save(dto, userDto.getId());
    }

    private void makeItemRequests() {
        UserDto userDto = UserMapper.toUserDto(requestor);
        userDto = userService.save(userDto);
        requestor.setId(userDto.getId());

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        requestService.save(dto, userDto.getId());
        requestService.save(dto, userDto.getId());
        requestService.save(dto, userDto.getId());
    }
}
