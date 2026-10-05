package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;

public final class Names {

    public static final int MAX_LENGTH = 100;

    private Names() {
    }

    public static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidValueException("Name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvalidValueException("Name must have at most %d characters".formatted(MAX_LENGTH));
        }
        return trimmed;
    }

    public static boolean sameName(String a, String b) {
        return a.equalsIgnoreCase(b);
    }
}
