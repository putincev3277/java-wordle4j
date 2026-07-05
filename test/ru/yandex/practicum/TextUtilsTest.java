package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest {

     @Test
    void normalize_throwsException_whenNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TextUtils.normalize(null)
        );
        assertEquals("Слово не может быть null", exception.getMessage());
    }


    @Test
    void normalize_trimAndLowercase() {
        assertEquals("слово", TextUtils.normalize("  СЛОВО  "));
    }

    @Test
    void normalize_replaceYo() {
        assertEquals("елка", TextUtils.normalize("ёлка"));
        assertEquals("медведь", TextUtils.normalize("мёдведь"));
    }

    @Test
    void normalize_mixedCaseAndSpaces() {
        assertEquals("привет", TextUtils.normalize(" ПрИвЕт "));
    }
}
