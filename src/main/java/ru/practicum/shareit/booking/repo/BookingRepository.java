package ru.practicum.shareit.booking.repo;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b where b.booker.id = ?1")
    List<Booking> findAllByBookerId(Long userId, Sort sort);

    @Query("select b from Booking b inner join Item i on b.item.id = i.id where i.owner.id = ?1")
    List<Booking> findAllByOwnerId(Long userId, Sort sort);

    @Query("select b from Booking b inner join Item i on b.item.id = ?1")
    List<Booking> findAllByItemId(Long itemId);
}
