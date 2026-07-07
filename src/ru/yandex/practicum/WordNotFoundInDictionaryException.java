package ru.yandex.practicum;

public class WordNotFoundInDictionaryException extends RuntimeException {
    public WordNotFoundInDictionaryException(String message) {
        super(message);
    }
}
