package ru.practicum.shareit.item.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentInfoDto {
    private Long id;
    @NotBlank
    private String text;
    @NotBlank
    private String authorName;
    @FutureOrPresent
    private LocalDateTime created;
}
