package ru.practicum.shareit.item;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingMapper;

public class ItemMapper {
    public static ItemShortDto toItemShortDto(@NotNull Item item) {
        return new ItemShortDto(
                item.getId(),
                item.getName()
        );
    }

    public static ItemDto toItemDto(@NotNull Item item, @NotNull Set<Comment> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                comments.stream()
                        .map(CommentMapper::toCommentInfoDto)
                        .collect(Collectors.toSet())
        );
    }

    public static ItemDto toItemDto(@NotNull Item item, @NotNull Booking lastBooking, @NotNull Booking nextBooking, @NotNull Set<Comment> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                BookingMapper.toBookingDto(lastBooking),
                BookingMapper.toBookingDto(nextBooking),
                comments.stream()
                        .map(CommentMapper::toCommentInfoDto)
                        .collect(Collectors.toSet())
        );
    }

    public static Item toItem(@NotNull ItemDto itemDto, @NotNull User owner) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner
        );
    }
}
