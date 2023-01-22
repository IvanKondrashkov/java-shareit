package ru.practicum.shareit.booking.repo;

import java.util.Set;
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
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import javax.persistence.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDbTest {
    private User owner;
    private User booker;
    private Booking booking;
    private Item item;
    private final EntityManager em;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @BeforeEach
    void init() {
        owner = new User(null, "Maks", "maks@mail.ru");
        booker = new User(null, "Djon", "djon@mail.ru");
        item = Item.builder()
                .name("Drill")
                .description("Drill MaxPro 2000")
                .available(true)
                .owner(owner)
                .build();
        booking = new Booking(null, LocalDateTime.now(), LocalDateTime.now().plusDays(5), BookingStatus.WAITING, item, booker);
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
        BookingInfoDto dto = makeBooking(booking);
        TypedQuery<Long> query = em.createQuery("select b.id from Booking as b where b.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        dto = bookingService.findById(owner.getId(), id);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getStart()).isBefore(LocalDateTime.now());
        assertThat(dto.getEnd()).isAfter(LocalDateTime.now());
        assertThat(dto.getStatus()).isEqualTo(booking.getStatus());
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getBooker()).isNotNull();
    }

    @Test
    void findAllByBookerId() {
        BookingInfoDto dto = makeBooking(booking);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.booker.id = :id", Booking.class);
        List<Booking> result = query
                .setParameter("id", booker.getId())
                .getResultList();

        List<BookingInfoDto> bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.ALL.name(), 0, 10);

        assertThat(result.size()).isEqualTo(bookings.size());
    }

    @Test
    void findAllByItemOwnerId() {
        BookingInfoDto dto = makeBooking(booking);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.item.owner.id = :id",
                Booking.class);
        List<Booking> result = query
                .setParameter("id", owner.getId())
                .getResultList();

        List<BookingInfoDto> bookings = bookingService.findAllByItemOwnerId(owner.getId(), BookingState.ALL.name(), 0, 10);

        assertThat(result.size()).isEqualTo(bookings.size());
    }

    @Test
    void save() {
        BookingInfoDto dto = makeBooking(booking);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.booker.email = :email", Booking.class);
        booking = query
                .setParameter("email", booker.getEmail())
                .getSingleResult();

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStart()).isBefore(LocalDateTime.now());
        assertThat(booking.getEnd()).isAfter(LocalDateTime.now());
        assertThat(booking.getStatus()).isEqualTo(dto.getStatus());
        assertThat(booking.getItem()).isNotNull();
        assertThat(booking.getBooker()).isNotNull();
    }

    @Test
    void update() {
        BookingInfoDto dto = makeBooking(booking);
        TypedQuery<Booking> query = em.createQuery("select b from Booking as b where b.id = :id", Booking.class);
        booking = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        Booking newBooking = new Booking(booking.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(5), BookingStatus.APPROVED, item, booker);
        dto = makeBooking(newBooking);
        dto = bookingService.update(owner.getId(), dto.getId(), true);

        assertThat(newBooking.getId()).isNotNull();
        assertThat(newBooking.getStart()).isBefore(LocalDateTime.now());
        assertThat(newBooking.getEnd()).isAfter(LocalDateTime.now());
        assertThat(newBooking.getStatus()).isEqualTo(dto.getStatus());
        assertThat(newBooking.getItem()).isNotNull();
        assertThat(newBooking.getBooker()).isNotNull();
    }

    @Test
    void deleteById() {
        BookingInfoDto dto = makeBooking(booking);
        TypedQuery<Long> query = em.createQuery("select b.id from Booking as b where b.id = :id", Long.class);
        Long id = query
                .setParameter("id", dto.getId())
                .getSingleResult();

        bookingService.deleteById(owner.getId(), id);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookingService.findById(owner.getId(), id);
        });

        String expectedMessage = exception.getMessage();
        String actualMessage = String.format("Booking with id=%d not found!", id);

        assertEquals(expectedMessage, actualMessage);
    }

    private BookingInfoDto makeBooking(Booking booking) {
        UserDto userDto = UserMapper.toUserDto(owner);
        userDto = userService.save(userDto);
        owner.setId(userDto.getId());

        userDto = UserMapper.toUserDto(booker);
        userDto = userService.save(userDto);
        booker.setId(userDto.getId());

        ItemDto itemDto = ItemMapper.toItemDto(item, Set.of());
        itemDto = itemService.save(itemDto, owner.getId());
        item.setId(itemDto.getId());

        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        return bookingService.save(bookingDto, booker.getId());
    }
}
