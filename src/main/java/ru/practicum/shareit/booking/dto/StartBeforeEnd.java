package ru.practicum.shareit.booking.dto;

import javax.validation.Constraint;
import java.lang.annotation.*;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartBeforeEndValidator.class)
@Documented
public @interface StartBeforeEnd {
    String message() default "Incorrect end booking date time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
