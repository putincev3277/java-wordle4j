package ru.yandex.practicum;

import java.io.Serial;

public class DictionaryLoadException extends Exception {
    @Serial
    private static final long serialVersionUID = -4958657947670858415L;

    public DictionaryLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
