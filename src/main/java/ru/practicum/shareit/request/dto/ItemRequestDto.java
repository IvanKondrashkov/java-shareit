package ru.practicum.shareit.request.dto;

import lombok.*;
import java.util.Set;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.marker.Create;

@Setter
@Getter
@Builder
@ToString
public class ItemRequestDto {
    private Long id;
    @NotBlank(groups = {Create.class})
    private String description;
    @FutureOrPresent(groups = {Create.class})
    private LocalDateTime created;
    private Set<ItemDto> items;
}
