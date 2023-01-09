package ru.practicum.shareit.booking.repo;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByItemOwnerId(Long userId);

    List<Booking> findAllByItemOwnerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime isBefore, LocalDateTime isAfter, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndEndIsBefore(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartIsAfter(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatusEquals(Long userId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByBookerId(Long userId, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime isBefore, LocalDateTime isAfter, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndIsBefore(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartIsAfter(Long userId, LocalDateTime date, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatusEquals(Long userId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByItemId(Long itemId);

    List<Booking> findByItemIn(List<Item> items, Sort sort);
}
