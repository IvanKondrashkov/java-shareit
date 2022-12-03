package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.marker.Create;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ItemDto {
    private Long id;
    @NotBlank(groups = {Create.class})
    private String name;
    @NotBlank(groups = {Create.class})
    private String description;
    @NotNull(groups = {Create.class})
    private Boolean available;
}
