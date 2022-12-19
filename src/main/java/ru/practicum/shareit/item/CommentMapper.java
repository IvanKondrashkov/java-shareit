package ru.practicum.shareit.item;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInfoDto;

public class CommentMapper {
    public static CommentDto toCommentDto(@NotNull Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText()
        );
    }

    public static CommentInfoDto toCommentInfoDto(@NotNull Comment comment) {
        return new CommentInfoDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static Comment toComment(@NotNull CommentDto commentDto, @NotNull Item item, @NotNull User author) {
        return new Comment(
                commentDto.getId(),
                commentDto.getText(),
                LocalDateTime.now(),
                item,
                author
        );
    }
}
