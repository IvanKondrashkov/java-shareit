package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import javax.persistence.EntityNotFoundException;
import ru.practicum.shareit.exception.UserConflictException;
import ru.practicum.shareit.exception.BookingStatusException;
import ru.practicum.shareit.exception.BookingStateExistsException;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    public BookingInfoDto findById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking bookingWrap = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final User booker = bookingWrap.getBooker();
        final User owner = bookingWrap.getItem().getOwner();

        if (booker.getId().equals(userWrap.getId()) || owner.getId().equals(userWrap.getId())) {
            return BookingMapper.toBookingInfoDto(bookingWrap);
        }
        throw new EntityNotFoundException(String.format("User with id=%d does not have the right to request extraction!", userId));
    }

    @Override
    public List<BookingInfoDto> findAllByBookerId(Long userId, String state, Integer from, Integer size) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        final MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(DESC, "start"));
        return findAllByStateBooker(userWrap.getId(), bookingState, pageRequest);
    }

    @Override
    public List<BookingInfoDto> findAllByItemOwnerId(Long userId, String state, Integer from, Integer size) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        final MyPageRequest pageRequest = new MyPageRequest(from, size, Sort.by(DESC, "start"));
        return findAllByStateOwner(userWrap.getId(), bookingState, pageRequest);
    }

    @Override
    @Transactional
    public BookingInfoDto save(BookingDto bookingDto, Long userId) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Item itemWrap = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", bookingDto.getItemId()))
        );
        if (itemWrap.getOwner().getId().equals(userWrap.getId())) {
            throw new UserConflictException(String.format("User userId=%d is the owner of the item!", userId));
        }
        if (!itemWrap.getAvailable()) {
            throw new BookingStatusException(String.format("Item available=%b, booking rejected!", itemWrap.getAvailable()));
        }
        final Booking booking = BookingMapper.toBooking(bookingDto, BookingStatus.WAITING, itemWrap, userWrap);
        final Booking bookingWrap = bookingRepository.save(booking);
        return BookingMapper.toBookingInfoDto(bookingWrap);
    }

    @Override
    @Transactional
    public BookingInfoDto update(Long userId, Long id, Boolean approved) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking bookingWrap = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final Item itemWrap = itemRepository.findById(bookingWrap.getItem().getId()).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", bookingWrap.getItem().getId()))
        );
        if (!itemWrap.getOwner().getId().equals(userWrap.getId())) {
            throw new UserConflictException(String.format("User userId=%d is not the owner of the item!", userId));
        }
        if (approved && bookingWrap.getStatus() == BookingStatus.WAITING) {
            bookingWrap.setStatus(BookingStatus.APPROVED);
        } else if (bookingWrap.getStatus() == BookingStatus.WAITING) {
            bookingWrap.setStatus(BookingStatus.REJECTED);
        } else {
            throw new BookingStatusException(String.format("Booking status=%s!", bookingWrap.getStatus()));
        }
        return BookingMapper.toBookingInfoDto(bookingWrap);
    }

    @Override
    @Transactional
    public void deleteById(Long userId, Long id) {
        final User userWrap = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking bookingWrap = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final User booker = bookingWrap.getBooker();
        final User owner = bookingWrap.getItem().getOwner();
        if (booker.getId().equals(userWrap.getId()) || owner.getId().equals(userWrap.getId())) {
            bookingRepository.deleteById(bookingWrap.getId());
        } else {
            throw new EntityNotFoundException(String.format("User with id=%d does not have the right to request deletion!", userId));
        }
    }

    private List<BookingInfoDto> findAllByStateOwner(Long userId, BookingState state, MyPageRequest pageRequest) {
        final LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        switch (state) {
            case CURRENT: {
                return bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, currentTime, currentTime, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case PAST: {
                return bookingRepository.findAllByItemOwnerIdAndEndIsBefore(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case FUTURE: {
                return bookingRepository.findAllByItemOwnerIdAndStartIsAfter(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case WAITING: {
                return bookingRepository.findAllByItemOwnerIdAndStatusEquals(userId, BookingStatus.WAITING, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case REJECTED: {
                return bookingRepository.findAllByItemOwnerIdAndStatusEquals(userId, BookingStatus.REJECTED, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            default: {
                return bookingRepository.findAllByItemOwnerId(userId, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
        }
    }

    private List<BookingInfoDto> findAllByStateBooker(Long userId, BookingState state, MyPageRequest pageRequest) {
        final LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        switch (state) {
            case CURRENT: {
                return bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(userId, currentTime, currentTime, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case PAST: {
                return bookingRepository.findAllByBookerIdAndEndIsBefore(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case FUTURE: {
                return bookingRepository.findAllByBookerIdAndStartIsAfter(userId, currentTime, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case WAITING: {
                return bookingRepository.findAllByBookerIdAndStatusEquals(userId, BookingStatus.WAITING, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            case REJECTED: {
                return bookingRepository.findAllByBookerIdAndStatusEquals(userId, BookingStatus.REJECTED, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
            default: {
                return bookingRepository.findAllByBookerId(userId, pageRequest).stream()
                        .map(BookingMapper::toBookingInfoDto)
                        .collect(toList());
            }
        }
    }
}
