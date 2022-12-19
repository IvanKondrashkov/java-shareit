package ru.practicum.shareit.item.dto;

import lombok.*;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ItemShortDto {
    private Long id;
    @NotNull
    private String name;
}
