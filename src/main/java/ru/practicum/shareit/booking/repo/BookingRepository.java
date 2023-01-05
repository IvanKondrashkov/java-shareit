package ru.practicum.shareit.booking.repo;

import java.util.List;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByItemOwnerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemOwnerId(Long userId);

    List<Booking> findAllByBookerId(Long userId, Pageable pageable);

    List<Booking> findAllByItemId(Long itemId);
}
