package ru.practicum.shareit.item.service;

import org.mockito.*;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.dto.CommentInfoDto;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.CommentForbiddenException;
import ru.practicum.shareit.exception.UserConflictException;
import javax.persistence.EntityNotFoundException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    private User owner;
    private User requestor;
    private ItemRequest request;
    private Item item;
    private Comment comment;
    private static final User BOOKER = new User(3L, "Bob", "bob@mail.ru");
    private static final Booking LAST_BOOKING = new Booking(1L, LocalDateTime.now().minusDays(5),
            LocalDateTime.now().minusDays(2), BookingStatus.WAITING, Item.builder().id(1L).build(), BOOKER);
    private static final Booking NEXT_BOOKING = new Booking(2L, LocalDateTime.now().plusDays(2),
            LocalDateTime.now().plusDays(5), BookingStatus.WAITING, Item.builder().id(1L).build(), BOOKER);
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

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
        comment = new Comment(1L, "Good item!", LocalDateTime.now(), item, owner);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        requestor = null;
        request = null;
        item = null;
        comment = null;
    }

    @ParameterizedTest
    @MethodSource("getBookings")
    void findById(Booking lastBooking, Booking nextBooking) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemOwnerId(owner.getId()))
                .thenReturn(lastBooking == null || nextBooking == null ? List.of() : List.of(lastBooking, nextBooking));

        ItemDto dto = itemService.findById(owner.getId(), item.getId());

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());

        if (lastBooking == null || nextBooking == null) {
            assertNull(dto.getLastBooking());
            assertNull(dto.getNextBooking());
        } else {
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(dto.getLastBooking())
                        .usingRecursiveComparison()
                        .isEqualTo(BookingMapper.toBookingDto(lastBooking));
            });
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(dto.getNextBooking())
                        .usingRecursiveComparison()
                        .isEqualTo(BookingMapper.toBookingDto(nextBooking));
            });
        }

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemOwnerId(owner.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.findById(userId, item.getId());
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
            itemService.findById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "", "Turbo drill", "DRILL", "2000"})
    void findAllByText(String text) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findAllByText(text)).thenReturn(text.isBlank() ? List.of() : List.of(item));

        List<ItemDto> items = itemService.findAllByText(owner.getId(), text);

        if (text.isBlank()) {
            assertEquals(0, items.size());
        } else {
            assertEquals(1, items.size());
        }

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findAllByText(text);
    }

    @Test
    void findAllTextByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.findAllByText(366L, "Drill");
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", 366L);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void findAll() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(item));

        List<ItemDto> items = itemService.findAll(owner.getId());

        assertEquals(items.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(2)).findAllByOwnerId(owner.getId());
    }

    @Test
    void findAllByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.findAll(owner.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void save() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);

        ItemDto dto = ItemMapper.toItemDto(item, request);
        ItemDto savedDto = itemService.save(dto, owner.getId());

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(dto)
                    .usingRecursiveComparison()
                    .isEqualTo(savedDto);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void saveByNotValidUserId() {
        ItemDto dto = ItemMapper.toItemDto(item, request);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.save(dto, owner.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void saveByNotValidRequestId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ItemDto dto = ItemMapper.toItemDto(item, request);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.save(dto, owner.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item request with id=%d not found!", request.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void update() {
        Item newItem = Item.builder()
                .id(item.getId())
                .name("Saw")
                .description("Electric saw")
                .available(false)
                .owner(owner)
                .request(request)
                .build();

        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemDto dto = ItemMapper.toItemDto(newItem, request);
        ItemDto savedItem = itemService.update(dto, owner.getId(), newItem.getId());

        assertEquals(savedItem.getId(), newItem.getId());
        assertEquals(savedItem.getName(), newItem.getName());
        assertEquals(savedItem.getDescription(), newItem.getDescription());
        assertEquals(savedItem.getAvailable(), newItem.getAvailable());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void updateByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            ItemDto dto = ItemMapper.toItemDto(item, request);
            itemService.update(dto, owner.getId(), dto.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void updateByNotValidItemId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            ItemDto dto = ItemMapper.toItemDto(item, request);
            itemService.update(dto, owner.getId(), dto.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item with id=%d not found!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void updateByRequestor() {
        Item newItem = Item.builder()
                .id(item.getId())
                .name("Saw")
                .description("Electric saw")
                .available(false)
                .owner(owner)
                .request(request)
                .build();

        Mockito.when(userRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            ItemDto dto = ItemMapper.toItemDto(newItem, request);
            itemService.update(dto, requestor.getId(), newItem.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User userId=%d is not the owner of the item!", requestor.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(requestor.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void deleteById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.deleteById(owner.getId(), item.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            itemService.deleteById(userId, item.getId());
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
            itemService.deleteById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void saveComment() {
        Mockito.when(userRepository.findById(BOOKER.getId())).thenReturn(Optional.of(BOOKER));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId())).thenReturn(List.of(LAST_BOOKING));
        Mockito.when(commentRepository.save(Mockito.any())).thenReturn(comment);

        CommentDto dto = CommentMapper.toCommentDto(comment);
        CommentInfoDto savedDto = itemService.saveComment(dto, BOOKER.getId(), item.getId());

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(dto)
                    .usingRecursiveComparison()
                    .isEqualTo(savedDto);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findById(BOOKER.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemId(item.getId());
        Mockito.verify(commentRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void saveCommentByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            CommentDto dto = CommentMapper.toCommentDto(comment);
            itemService.saveComment(dto, owner.getId(), item.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", owner.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void saveCommentByNotValidItemId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            CommentDto dto = CommentMapper.toCommentDto(comment);
            itemService.saveComment(dto, owner.getId(), item.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item with id=%d not found!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void saveCommentByOwner() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemId(item.getId())).thenReturn(List.of(LAST_BOOKING));

        CommentForbiddenException exception = assertThrows(CommentForbiddenException.class, () -> {
            CommentDto dto = CommentMapper.toCommentDto(comment);
            itemService.saveComment(dto, owner.getId(), item.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User userId=%d is not the booker of the item!", owner.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemId(item.getId());
    }

    private static Stream<Arguments> getBookings() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(LAST_BOOKING, NEXT_BOOKING)
        );
    }
}
