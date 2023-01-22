package ru.practicum.shareit.booking.dto;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.BookerDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BookingInfoDto {
    private Long id;
    @FutureOrPresent
    @NotNull
    private LocalDateTime start;
    @Future
    @NotNull
    private LocalDateTime end;
    @NotNull
    private BookingStatus status;
    @NotNull
    private ItemDto item;
    @NotNull
    private BookerDto booker;
}
