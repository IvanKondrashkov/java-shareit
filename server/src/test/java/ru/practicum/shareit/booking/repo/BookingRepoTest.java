package ru.practicum.shareit.booking.repo;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepoTest {
    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void init() {
        owner = new User(null, "Nikolas", "nik@mail.ru");
        booker = new User(null, "Djon", "djon@mail.ru");
        item = Item.builder()
                .id(null)
                .name("Drill")
                .description("Drill 2000 MaxPro")
                .available(true)
                .owner(owner)
                .build();
        booking = new Booking(null, LocalDateTime.now(), LocalDateTime.now().plusDays(2), BookingStatus.WAITING, item, booker);

        em.persist(owner);
        em.persist(booker);
        em.persist(item);
        em.persist(booking);
    }

    @AfterEach
    void tearDown() {
        owner = em.find(User.class, owner.getId());
        em.remove(owner);
        booker = em.find(User.class, booker.getId());
        em.remove(booker);
        item = em.find(Item.class, item.getId());
        em.remove(item);
        booking = em.find(Booking.class, booking.getId());
        em.remove(booking);
        em.flush();
        em.clear();
    }

    @ParameterizedTest
    @MethodSource("getPageable")
    void findAllByItemOwnerId(Pageable pageable) {
        List<Booking> bookings = pageable == null ?
                bookingRepository.findAllByItemOwnerId(owner.getId()) :
                bookingRepository.findAllByItemOwnerId(owner.getId(), pageable);

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter() {
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                owner.getId(), LocalDateTime.now(), LocalDateTime.now(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByItemOwnerIdAndEndIsBefore() {
        booking.setEnd(LocalDateTime.now().minusDays(2));
        em.merge(booking);
        em.flush();

        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdAndEndIsBefore(
                owner.getId(), LocalDateTime.now(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByItemOwnerIdAndStartIsAfter() {
        booking.setStart(LocalDateTime.now().plusDays(2));
        em.merge(booking);
        em.flush();

        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdAndStartIsAfter(
                owner.getId(), LocalDateTime.now(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByItemOwnerIdAndStatusEquals() {
        booking.setStatus(BookingStatus.APPROVED);
        em.merge(booking);
        em.flush();

        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdAndStatusEquals(
                owner.getId(), BookingStatus.APPROVED, Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByBookerId() {
        List<Booking> bookings = bookingRepository.findAllByBookerId(booker.getId(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByBookerIdAndStartIsBeforeAndEndIsAfter() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(
                booker.getId(), LocalDateTime.now(), LocalDateTime.now(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByBookerIdAndEndIsBefore() {
        booking.setEnd(LocalDateTime.now().minusDays(2));
        em.merge(booking);
        em.flush();

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndIsBefore(
                booker.getId(), LocalDateTime.now(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByBookerIdAndStartIsAfter() {
        booking.setStart(LocalDateTime.now().plusDays(2));
        em.merge(booking);
        em.flush();

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsAfter(
                booker.getId(), LocalDateTime.now(), Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByBookerIdAndStatusEquals() {
        booking.setStatus(BookingStatus.APPROVED);
        em.merge(booking);
        em.flush();

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStatusEquals(
                booker.getId(), BookingStatus.APPROVED, Pageable.unpaged());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByItemId() {
        List<Booking> bookings = bookingRepository.findAllByItemId(item.getId());

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    @Test
    void findByItemInAndStatusEquals() {
        List<Booking> bookings = bookingRepository.findByItemInAndStatusEquals(List.of(item), BookingStatus.WAITING,
                Sort.by(Sort.Direction.DESC, "start"));

        assertNotNull(owner.getId());
        assertNotNull(booker.getId());
        assertNotNull(item.getId());
        assertNotNull(booking.getId());
        assertEquals(1, bookings.size());
    }

    private static Stream<Arguments> getPageable() {
        return Stream.of(
                Arguments.of((Pageable) null),
                Arguments.of(Pageable.unpaged())
        );
    }
}
