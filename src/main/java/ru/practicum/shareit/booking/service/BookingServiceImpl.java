package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    public BookingInfoDto findById(Long userId, Long id) {
        final User u = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking b = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final User booker = b.getBooker();
        final User owner = b.getItem().getOwner();

        if (booker.getId().equals(u.getId()) || owner.getId().equals(u.getId())) {
            return BookingMapper.toBookingDtoInfo(b);
        }
        throw new EntityNotFoundException(String.format("User with id=%d does not have the right to request extraction!", userId));
    }

    @Override
    public List<BookingInfoDto> findAllByBooker(Long userId, String state) {
        final User booker = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        List<Booking> bookings = bookingRepository.findAllByBookerId(booker.getId(), Sort.by(Sort.Direction.DESC, "start"));
        return findAllByState(bookingState, bookings);
    }

    @Override
    public List<BookingInfoDto> findAllByOwner(Long userId, String state) {
        final User owner = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final BookingState bookingState = BookingState.from(state);
        if (bookingState == null) {
            throw new BookingStateExistsException("Unknown state: UNSUPPORTED_STATUS");
        }
        List<Booking> bookings = bookingRepository.findAllByOwnerId(owner.getId(), Sort.by(Sort.Direction.DESC, "start"));
        return findAllByState(bookingState, bookings);
    }

    @Override
    @Transactional
    public BookingInfoDto save(BookingDto bookingDto, Long userId) {
        final User u = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Item i = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", bookingDto.getItemId()))
        );
        if (i.getOwner().getId().equals(u.getId())) {
            throw new UserConflictException(String.format("User userId=%d is the owner of the item!", userId));
        }
        if (!i.getAvailable()) {
            throw new BookingStatusException(String.format("Item available=%b, booking rejected!", i.getAvailable()));
        }
        final Booking booking = BookingMapper.toBooking(bookingDto, BookingStatus.WAITING, i, u);
        final Booking b = bookingRepository.save(booking);
        return BookingMapper.toBookingDtoInfo(b);
    }

    @Override
    @Transactional
    public BookingInfoDto update(Long userId, Long id, String approved) {
        final User u = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking b = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final Item i = itemRepository.findById(b.getItem().getId()).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with id=%d not found!", b.getItem().getId()))
        );
        if (!i.getOwner().getId().equals(u.getId())) {
            throw new UserConflictException(String.format("User userId=%d is not the owner of the item!", userId));
        }

        if (Boolean.parseBoolean(approved) && b.getStatus() == BookingStatus.WAITING) {
            b.setStatus(BookingStatus.APPROVED);
        } else if (b.getStatus() == BookingStatus.WAITING) {
            b.setStatus(BookingStatus.REJECTED);
        } else {
            throw new BookingStatusException(String.format("Booking status=%s!", b.getStatus()));
        }

        bookingRepository.save(b);
        return BookingMapper.toBookingDtoInfo(b);
    }

    @Override
    @Transactional
    public void deleteById(Long userId, Long id) {
        final User u = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id=%d not found!", userId))
        );
        final Booking b = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Booking with id=%d not found!", id))
        );
        final User booker = b.getBooker();
        final User owner = b.getItem().getOwner();

        if (booker.getId().equals(u.getId()) || owner.getId().equals(u.getId())) {
            bookingRepository.deleteById(b.getId());
        } else {
            throw new EntityNotFoundException(String.format("User with id=%d does not have the right to request deletion!", userId));
        }
    }

    private List<BookingInfoDto> findAllByState(BookingState state, List<Booking> bookings) {
        final LocalDateTime currentTime = LocalDateTime.now();

        switch (state) {
            case ALL: {
                return bookings.stream()
                        .map(BookingMapper::toBookingDtoInfo)
                        .collect(Collectors.toList());
            }
            case CURRENT: {
                return bookings.stream()
                        .filter(it -> it.getStart().isBefore(currentTime) && it.getEnd().isAfter(currentTime))
                        .map(BookingMapper::toBookingDtoInfo)
                        .collect(Collectors.toList());
            }
            case PAST: {
                return bookings.stream()
                        .filter(it -> it.getEnd().isBefore(currentTime))
                        .map(BookingMapper::toBookingDtoInfo)
                        .collect(Collectors.toList());
            }
            case FUTURE: {
                return bookings.stream()
                        .filter(it -> it.getStart().isAfter(currentTime))
                        .map(BookingMapper::toBookingDtoInfo)
                        .collect(Collectors.toList());
            }
            case WAITING: {
                return bookings.stream()
                        .filter(it -> it.getStatus() == BookingStatus.WAITING)
                        .map(BookingMapper::toBookingDtoInfo)
                        .collect(Collectors.toList());
            }
            case REJECTED: {
                return bookings.stream()
                        .filter(it -> it.getStatus() == BookingStatus.REJECTED)
                        .map(BookingMapper::toBookingDtoInfo)
                        .collect(Collectors.toList());
            }
        }
        return null;
    }
}
