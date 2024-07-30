package io.github.guqing.share;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException extends ResponseStatusException {
    public NotFoundException(String reason) {
        this(reason, null);
    }

    public NotFoundException(String reason, Throwable cause) {
        super(HttpStatus.NOT_FOUND, reason, cause);
    }
}
