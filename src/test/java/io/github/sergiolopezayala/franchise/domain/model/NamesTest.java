package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NamesTest {

    @Test
    void trimsName() {
        assertThat(Names.normalize("  Main street  ")).isEqualTo("Main street");
    }

    @Test
    void acceptsMaxLength() {
        String name = "a".repeat(Names.MAX_LENGTH);
        assertThat(Names.normalize(name)).isEqualTo(name);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rejectsBlank(String name) {
        assertThatThrownBy(() -> Names.normalize(name))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("Name must not be blank");
    }

    @Test
    void rejectsTooLong() {
        assertThatThrownBy(() -> Names.normalize("a".repeat(Names.MAX_LENGTH + 1)))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void comparesIgnoringCase() {
        assertThat(Names.sameName("Coffee", "coffee")).isTrue();
        assertThat(Names.sameName("Coffee", "Tea")).isFalse();
    }
}
