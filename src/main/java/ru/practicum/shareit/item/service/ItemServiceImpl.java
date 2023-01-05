package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.BookingStateExistsException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInfoDto;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import javax.persistence.EntityNotFoundException;
import ru.practicum.shareit.exception.UserConflictException;
import ru.practicum.shareit.exception.CommentForbiddenException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDto findById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Item itemWrap = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", id))
        );
        final List<Booking> bookings = bookingRepository.findAllByItemOwnerId(userWrap.getId());
        final Booking lastBooking = findBookingByStatePastOrFuture(BookingState.PAST, bookings);
        final Booking nextBooking = findBookingByStatePastOrFuture(BookingState.FUTURE, bookings);
        final Set<Comment> comments = commentRepository.findAllByItemId(itemWrap.getId());
        return lastBooking == null || nextBooking == null ?
                ItemMapper.toItemDto(itemWrap, comments) :
                ItemMapper.toItemDto(itemWrap, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemDto> findAllByText(Long userId, String text) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        List<ItemDto> items = itemRepository.findAllByText(text).stream()
                .map(it -> ItemMapper.toItemDto(it, commentRepository.findAllByItemId(it.getId())))
                .collect(Collectors.toList());
        return text.isBlank() ? List.of() : items;
    }

    @Override
    public List<ItemDto> findAll(Long userId) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        Comparator<ItemDto> comparator = new Comparator<ItemDto>() {
            @Override
            public int compare(ItemDto o1, ItemDto o2) {
                final BookingDto lb1 = o1.getLastBooking();
                final BookingDto lb2 = o2.getLastBooking();

                if (lb1 == null && lb2 == null) {
                    return 0;
                }
                if (lb1 == null) {
                    return 1;
                }
                if (lb2 == null) {
                    return -1;
                }
                return lb1.getStart().compareTo(lb2.getStart());
            }
        };
        return itemRepository.findAllByOwnerId(userWrap.getId()).stream()
                .map(it -> ItemMapper.toItemDto(it, commentRepository.findAllByItemId(it.getId())))
                .peek(it -> {
                    final List<Booking> bookings = bookingRepository.findAllByItemId(it.getId());
                    final Booking lastBooking = findBookingByStatePastOrFuture(BookingState.PAST, bookings);
                    final Booking nextBooking = findBookingByStatePastOrFuture(BookingState.FUTURE, bookings);
                    if (lastBooking != null && nextBooking != null) {
                        it.setLastBooking(BookingMapper.toBookingDto(lastBooking));
                        it.setNextBooking(BookingMapper.toBookingDto(nextBooking));
                    }
                })
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto save(ItemDto itemDto, Long userId) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        ItemRequest requestWrap = null;
        if (itemDto.getRequestId() != null) {
            requestWrap =  requestRepository.findById(itemDto.getRequestId()).orElseThrow(
                    () -> new EntityNotFoundException(String.format("Item request with id=%d not found!", itemDto.getRequestId()))
            );
        }
        final Item item = requestWrap == null ? ItemMapper.toItem(itemDto, userWrap) : ItemMapper.toItem(itemDto, userWrap, requestWrap);
        final Item itemWrap = itemRepository.save(item);
        final Set<Comment> comments = commentRepository.findAllByItemId(itemWrap.getId());
        return itemWrap.getRequest() == null ? ItemMapper.toItemDto(itemWrap, comments) : ItemMapper.toItemDto(itemWrap, itemWrap.getRequest());
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Item itemWrap = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", id))
        );
        if (!itemWrap.getOwner().getId().equals(userId)) {
            throw new UserConflictException(String.format("User userId=%d is not the owner of the item!", userId)
            );
        }
        final Item item = ItemMapper.toItem(itemDto, userWrap);
        Optional.ofNullable(item.getName()).ifPresent(itemWrap::setName);
        Optional.ofNullable(item.getDescription()).ifPresent(itemWrap::setDescription);
        Optional.ofNullable(item.getAvailable()).ifPresent(itemWrap::setAvailable);
        final Set<Comment> comments = commentRepository.findAllByItemId(itemWrap.getId());
        itemRepository.save(itemWrap);
        return ItemMapper.toItemDto(itemWrap, comments);
    }

    @Override
    @Transactional
    public void deleteById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Item itemWrap = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", id))
        );
        itemRepository.deleteById(itemWrap.getId());
    }

    @Override
    @Transactional
    public CommentInfoDto saveComment(CommentDto commentDto, Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Item itemWrap = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", id))
        );
        final List<Booking> bookings = bookingRepository.findAllByItemId(itemWrap.getId());
        final Booking booking = findBookingByStatePastOrFuture(BookingState.PAST, bookings);

        if (!booking.getBooker().getId().equals(userId)) {
            throw new CommentForbiddenException(String.format("User userId=%d is not the booker of the item!",
                    userId)
            );
        }
        final Comment comment = CommentMapper.toComment(commentDto, itemWrap, userWrap);
        final Comment commentWrap = commentRepository.save(comment);
        return CommentMapper.toCommentInfoDto(commentWrap);
    }

    private Booking findBookingByStatePastOrFuture(BookingState state, List<Booking> bookings) {
        final LocalDateTime currentTime = LocalDateTime.now();

        switch (state) {
            case PAST: {
                return bookings.stream()
                        .filter(it -> it.getStart().isBefore(currentTime))
                        .findFirst().orElse(null);
            }
            case FUTURE: {
                return bookings.stream()
                        .filter(it -> it.getStart().isAfter(currentTime))
                        .findFirst().orElse(null);
            }
            default: {
                throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
            }
        }
    }
}
