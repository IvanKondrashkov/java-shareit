package ru.practicum.shareit.item.dto;

import lombok.*;
import javax.validation.constraints.*;
import ru.practicum.shareit.marker.Create;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentDto {
    private Long id;
    @NotBlank(groups = {Create.class})
    private String text;
}
