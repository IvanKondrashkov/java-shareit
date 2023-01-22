package ru.practicum.shareit.item.dto;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentInfoDto {
    @NotBlank
    private String text;
    @NotBlank
    private String authorName;
    @FutureOrPresent
    private LocalDateTime created;
}
