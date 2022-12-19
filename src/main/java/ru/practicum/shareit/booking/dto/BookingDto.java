package ru.practicum.shareit.booking.dto;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import ru.practicum.shareit.marker.Create;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@StartBeforeEnd(groups = {Create.class})
public class BookingDto {
    private Long id;
    @FutureOrPresent(groups = {Create.class})
    @NotNull(groups = {Create.class})
    private LocalDateTime start;
    @Future(groups = {Create.class})
    @NotNull(groups = {Create.class})
    private LocalDateTime end;
    @NotNull(groups = {Create.class})
    private Long itemId;
    private Long bookerId;
}
