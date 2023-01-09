package ru.practicum.shareit.item.dto;

import lombok.*;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.marker.Create;

@Setter
@Getter
@Builder
@ToString
public class ItemDto {
    private Long id;
    @NotBlank(groups = {Create.class})
    private String name;
    @NotBlank(groups = {Create.class})
    private String description;
    @NotNull(groups = {Create.class})
    private Boolean available;
    private Long requestId;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private Set<CommentInfoDto> comments;
}
