package ru.practicum.shareit.booking.dto;

import lombok.*;
import java.time.LocalDateTime;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.BookerDto;
import ru.practicum.shareit.booking.model.BookingStatus;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BookingInfoDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private ItemDto item;
    private BookerDto booker;
}
