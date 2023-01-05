package ru.practicum.shareit.item;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingMapper;

public class ItemMapper {
    public static ItemDto toItemDto(@NotNull Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    public static ItemDto toItemDto(@NotNull Item item, @NotNull ItemRequest request) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(request.getId())
                .build();
    }

    public static ItemDto toItemDto(@NotNull Item item, @NotNull Set<Comment> comments) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(comments.stream()
                        .map(CommentMapper::toCommentInfoDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static ItemDto toItemDto(@NotNull Item item, @NotNull Booking lastBooking, @NotNull Booking nextBooking, @NotNull Set<Comment> comments) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(BookingMapper.toBookingDto(lastBooking))
                .nextBooking(BookingMapper.toBookingDto(nextBooking))
                .comments(comments.stream()
                        .map(CommentMapper::toCommentInfoDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Item toItem(@NotNull ItemDto itemDto, @NotNull User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();
    }

    public static Item toItem(@NotNull ItemDto itemDto, @NotNull User owner, @NotNull ItemRequest request) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}
