package ru.yandex.practicum;

public class TextUtils {
    public static String normalize(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Слово не может быть null");
        }
        return word.trim().toLowerCase().replace('ё', 'е');
    }
}
