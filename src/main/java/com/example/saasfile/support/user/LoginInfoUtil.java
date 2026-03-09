package com.example.saasfile.support.user;

import com.example.saasfile.support.exception.SupException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public final class LoginInfoUtil {

    private LoginInfoUtil() {
    }

    public static LoginInfoUserDTO getCurrentUser() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new SupException("No request context available");
        }

        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String userIdHeader = firstNonBlank(request, "X-User-Id", "x-user-id", "user-id");
        String userNameHeader = firstNonBlank(request, "X-User-Name", "x-user-name", "user-name");
        if (userIdHeader == null && userNameHeader == null) {
            throw new SupException("No login user found in request headers");
        }

        LoginInfoUserDTO user = new LoginInfoUserDTO();
        if (userIdHeader != null) {
            try {
                user.setId(Long.parseLong(userIdHeader));
            } catch (NumberFormatException ex) {
                throw new SupException("Invalid user id header");
            }
        }
        user.setUsername(userNameHeader == null ? "anonymous" : userNameHeader);
        return user;
    }

    private static String firstNonBlank(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}
