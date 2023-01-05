package ru.practicum.shareit.request.service;

import org.mockito.*;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.time.LocalDateTime;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import ru.practicum.shareit.MyPageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import javax.persistence.EntityNotFoundException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Item item;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @BeforeEach
    void init() {
        owner = new User(1L, "Nikolas", "nik@mail.ru");
        requestor = new User(2L, "Djon", "djon@mail.ru");
        request = new ItemRequest(1L, "Drill 2000 MaxPro", LocalDateTime.now(), requestor);
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Drill 2000 MaxPro")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
    }

    @AfterEach
    void tearDown() {
        owner = null;
        requestor = null;
        request = null;
        item = null;
    }


    @Test
    void findById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        Mockito.when(itemRepository.findAllByRequestId(request.getId())).thenReturn(Set.of(item));

        ItemRequestDto dto = requestService.findById(owner.getId(), request.getId());

        assertEquals(dto.getId(), request.getId());
        assertEquals(dto.getDescription(), request.getDescription());
        assertEquals(dto.getCreated(), request.getCreated());
        assertEquals(dto.getItems().size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findAllByRequestId(request.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.findById(userId, request.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.findById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void findAll() {
        Mockito.when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        Mockito.when(requestRepository.findAllByRequestorId(requestor.getId())).thenReturn(List.of(request));

        List<ItemRequestDto> requests = requestService.findAll(requestor.getId());

        assertEquals(requests.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(requestor.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findAllByRequestorId(requestor.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findAllByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.findAll(userId);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void findByPage() {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findAllByRequestorIdNot(owner.getId(), pageRequest)).thenReturn(List.of(request));

        List<ItemRequestDto> requests = requestService.findByPage(owner.getId(), 0, 10);

        assertEquals(requests.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findAllByRequestorIdNot(owner.getId(), pageRequest);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByPageNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.findByPage(userId, 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void save() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.save(Mockito.any())).thenReturn(request);

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        ItemRequestDto savedDto = requestService.save(dto, owner.getId());

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(dto)
                    .usingRecursiveComparison()
                    .isEqualTo(savedDto);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void saveByNotValidUserId(Long userId) {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.save(dto, userId);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        requestService.deleteById(owner.getId(), request.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.deleteById(userId, request.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteByNotValidId(Long id) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            requestService.deleteById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }
}
