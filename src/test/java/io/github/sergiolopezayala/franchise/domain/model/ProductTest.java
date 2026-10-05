package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void createsProductWithTrimmedName() {
        Product product = new Product("p1", " Coffee ", 5);

        assertThat(product.name()).isEqualTo("Coffee");
        assertThat(product.stock()).isEqualTo(5);
    }

    @Test
    void rejectsNegativeStock() {
        assertThatThrownBy(() -> new Product("p1", "Coffee", -1))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void acceptsZeroStock() {
        assertThat(new Product("p1", "Coffee", 0).stock()).isZero();
    }

    @Test
    void renameAndWithStockReturnNewInstances() {
        Product product = new Product("p1", "Coffee", 5);

        assertThat(product.rename("Tea")).isEqualTo(new Product("p1", "Tea", 5));
        assertThat(product.withStock(9)).isEqualTo(new Product("p1", "Coffee", 9));
        assertThat(product).isEqualTo(new Product("p1", "Coffee", 5));
    }
}
