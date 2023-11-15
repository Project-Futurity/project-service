package com.alex.futurity.projectserver.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@UtilityClass
public class DateUtils {
    public static boolean isInPast(@NonNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.isBefore(now());
    }

    public static boolean isInFuture(@NonNull ZonedDateTime zonedDateTime) {
        return !isInPast(zonedDateTime);
    }

    public ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}
