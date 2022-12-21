package ru.practicum.shareit.booking.service;

import java.util.List;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

public interface BookingService {
    /**
     * Find booking by id.
     * @param userId User id.
     * @param id Booking id.
     * @return BookingInfoDto.
     */
    BookingInfoDto findById(Long userId, Long id);

    /**
     * Find all booking by booker, sort desc start datetime.
     * @param userId User id.
     * @param state Booking state.
     * @return List booking info dto.
     */
    List<BookingInfoDto> findAllByBookerId(Long userId, String state);

    /**
     * Find all booking by owner, sort desc start datetime.
     * @param userId User id.
     * @param state Booking state.
     * @return List booking info dto.
     */
    List<BookingInfoDto> findAllByItemOwnerId(Long userId, String state);

    /**
     * Create booking.
     * @param bookingDto Entity.
     * @param userId User id.
     * @return BookingInfoDto.
     */
    BookingInfoDto save(BookingDto bookingDto, Long userId);

    /**
     * Update booking by id. Confirmation or rejection of a booking request.
     * @param userId User id.
     * @param id Booking id.
     * @param approved The parameter can take true or false.
     * @return BookingInfoDto.
     */
    BookingInfoDto update(Long userId, Long id, String approved);

    /**
     * Delete booking by id.
     * @param userId User id.
     * @param id Booking id.
     */
    void deleteById(Long userId, Long id);
}
