package ru.practicum.shareit.booking.dto;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
public class StartBeforeEndValidator implements ConstraintValidator<StartBeforeEnd, BookingDto> {
    @Override
    public boolean isValid(BookingDto bookingDto, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        log.info("Validation start={} date time is before end={} date time", start, end);
        return start.isBefore(end);
    }
}
