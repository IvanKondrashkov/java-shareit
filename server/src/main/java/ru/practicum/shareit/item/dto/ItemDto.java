package ru.practicum.shareit.item.dto;

import lombok.*;
import java.util.Set;
import ru.practicum.shareit.booking.dto.BookingDto;

@Setter
@Getter
@Builder
@ToString
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private Set<CommentInfoDto> comments;
}
