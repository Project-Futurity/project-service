package com.alex.futurity.projectserver.context;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

public class UserContext {
    public static final String USER = "user";

    public static User getUser() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(requestAttributes -> requestAttributes.getAttribute(USER, RequestAttributes.SCOPE_REQUEST))
                .map(User.class::cast)
                .orElse(null);
    }

    public static Long getUserId() {
        return getUser().getUserId();
    }

    public static boolean hasTelegram() {
        return getUser().isHasTelegram();
    }
}
