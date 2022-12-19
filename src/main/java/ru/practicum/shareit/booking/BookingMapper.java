package ru.practicum.shareit.booking;

import javax.validation.constraints.NotNull;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.BookingStatus;

public class BookingMapper {
    public static BookingDto toBookingDto(@NotNull Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId(),
                booking.getBooker().getId()
        );
    }

    public static BookingInfoDto toBookingDtoInfo(@NotNull Booking booking) {
        return new BookingInfoDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                ItemMapper.toItemShortDto(booking.getItem()),
                UserMapper.toBookerDto(booking.getBooker())
        );
    }

    public static Booking toBooking(@NotNull BookingDto bookingDto, @NotNull BookingStatus status, @NotNull Item item, @NotNull User booker) {
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                status,
                item,
                booker
        );
    }
}
