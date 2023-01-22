package ru.practicum.shareit.user.dto;

import lombok.*;
import javax.validation.constraints.*;
import ru.practicum.shareit.marker.Create;
import ru.practicum.shareit.marker.Update;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDto {
    private Long id;
    @NotBlank(groups = {Create.class})
    private String name;
    @Email(groups = {Create.class, Update.class})
    @NotNull(groups = {Create.class})
    private String email;
}
