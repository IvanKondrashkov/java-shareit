package ru.practicum.shareit.booking.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.BookingStateExistsException;
import ru.practicum.shareit.exception.BookingStatusException;
import ru.practicum.shareit.exception.UserConflictException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import javax.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Captor
    private ArgumentCaptor<Booking> captor;

    @BeforeEach
    void init() {
        owner = new User(1L, "Nikolas", "nik@mail.ru");
        booker = new User(2L, "Djon", "djon@mail.ru");
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Drill 2000 MaxPro")
                .available(true)
                .owner(owner)
                .build();
        booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2), BookingStatus.WAITING, item,
                booker);
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        item = null;
        booking = null;
    }

    @Test
    void findById() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingInfoDto dto = bookingService.findById(owner.getId(), booking.getId());

        assertEquals(dto.getId(), booking.getId());
        assertEquals(dto.getStart(), booking.getStart());
        assertEquals(dto.getEnd(), booking.getEnd());
        assertEquals(dto.getStatus(), booking.getStatus());
        assertNotNull(dto.getItem().getId());
        assertNotNull(dto.getBooker());

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findById(userId, booking.getId());
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
            bookingService.findById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Booking with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findByOtherUserNotValidUserId(Long userId) {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findById(userId, booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d does not have the right to request extraction!", userId);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void findAllByBookerId(BookingState state) {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        switch (state) {
            case PAST: {
                booking.setEnd(LocalDateTime.now().minusDays(5));
            }
            case FUTURE: {
                booking.setStart(LocalDateTime.now().plusDays(5));
            }
            case REJECTED: {
                booking.setStatus(BookingStatus.REJECTED);
            }
        }

        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllByBookerId(booker.getId(), pageRequest)).thenReturn(List.of(booking));

        List<BookingInfoDto> bookings = bookingService.findAllByBookerId(booker.getId(), state.name(), 0, 10);

        assertEquals(bookings.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByBookerId(booker.getId(), pageRequest);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findAllByBookerNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findAllByBookerId(userId, BookingState.ALL.name(), 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "", "PPS", "VENICE"})
    void findAllByBookerNotValidBookingState(String state) {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        BookingStateExistsException exception = assertThrows(BookingStateExistsException.class, () -> {
            bookingService.findAllByBookerId(booker.getId(), state, 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = "Unknown state: UNSUPPORTED_STATUS";

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void findAllByItemOwnerId(BookingState state) {
        MyPageRequest pageRequest = new MyPageRequest(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        switch (state) {
            case PAST: {
                booking.setEnd(LocalDateTime.now().minusDays(5));
            }
            case FUTURE: {
                booking.setStart(LocalDateTime.now().plusDays(5));
            }
            case REJECTED: {
                booking.setStatus(BookingStatus.REJECTED);
            }
        }

        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerId(owner.getId(), pageRequest)).thenReturn(List.of(booking));

        List<BookingInfoDto> bookings = bookingService.findAllByItemOwnerId(owner.getId(), BookingState.ALL.name(), 0, 10);

        assertEquals(bookings.size(), 1);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemOwnerId(owner.getId(), pageRequest);
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void findAllByItemOwnerNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findAllByItemOwnerId(userId, BookingState.ALL.name(), 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "", "PPS", "VENICE"})
    void findAllByItemOwnerNotValidBookingState(String state) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        BookingStateExistsException exception = assertThrows(BookingStateExistsException.class, () -> {
            bookingService.findAllByItemOwnerId(owner.getId(), state, 0, 10);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = "Unknown state: UNSUPPORTED_STATUS";

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void save() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        BookingDto dto = BookingMapper.toBookingDto(booking);
        BookingInfoDto savedDto = bookingService.save(dto, booker.getId());

        assertEquals(savedDto.getId(), dto.getId());
        assertEquals(savedDto.getStart(), dto.getStart());
        assertEquals(savedDto.getEnd(), dto.getEnd());
        assertNotNull(savedDto.getItem());
        assertNotNull(savedDto.getBooker());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void saveByOwner() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            BookingDto dto = BookingMapper.toBookingDto(booking);
            bookingService.save(dto, owner.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User userId=%d is the owner of the item!", owner.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void saveByNotValidAvailable() {
        item.setAvailable(false);
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingStatusException exception = assertThrows(BookingStatusException.class, () -> {
            BookingDto dto = BookingMapper.toBookingDto(booking);
            bookingService.save(dto, booker.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item available=%b, booking rejected!", item.getAvailable());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
    }

    @Test
    void saveByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.toBookingDto(booking);
            bookingService.save(dto, booker.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", booker.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void saveByNotValidItemId() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            BookingDto dto = BookingMapper.toBookingDto(booking);
            bookingService.save(dto, booker.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item with id=%d not found!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void update(String approved) {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        bookingService.update(owner.getId(), booking.getId(), approved);

        Mockito.verify(bookingRepository).save(captor.capture());
        Booking savedBooking = captor.getValue();

        assertEquals(savedBooking.getId(), booking.getId());
        assertEquals(savedBooking.getStart(), booking.getStart());
        assertEquals(savedBooking.getEnd(), booking.getEnd());

        if (Boolean.parseBoolean(approved) && booking.getStatus() == BookingStatus.WAITING) {
            assertEquals(savedBooking.getStatus(), BookingStatus.APPROVED);
        } else if (booking.getStatus() == BookingStatus.WAITING) {
            assertEquals(savedBooking.getStatus(), BookingStatus.REJECTED);
        }

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void updateByNotValidUserId(Long userId) {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.update(userId, booking.getId(), "false");
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", userId);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void updateByBooker() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            bookingService.update(booker.getId(), booking.getId(), "false");
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User userId=%d is not the owner of the item!", booker.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    void updateByNotValidItemId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.update(owner.getId(), booking.getId(), "false");
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Item with id=%d not found!", item.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    void updateByNotValidId() {
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.update(owner.getId(), booking.getId(), "false");
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Booking with id=%d not found!", booking.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(owner.getId());
    }

    @Test
    void deleteById() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        bookingService.deleteById(booker.getId(), booking.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }

    @Test
    void deleteByNotValidUserId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteById(booker.getId(), booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d not found!", booker.getId());

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteByNotValidId() {
        Mockito.when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteById(booker.getId(), booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Booking with id=%d not found!", booking.getId());

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(booker.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 32, 999})
    void deleteByOtherUserNotValidUserId(Long userId) {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.deleteById(userId, booking.getId());
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("User with id=%d does not have the right to request deletion!", userId);

        assertEquals(expectedMessage, actualMessage);

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
    }
}
