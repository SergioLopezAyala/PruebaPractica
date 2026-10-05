package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;

public record Product(String id, String name, int stock) {

    public Product {
        name = Names.normalize(name);
        if (stock < 0) {
            throw new InvalidValueException("Stock must be greater than or equal to 0");
        }
    }

    public Product rename(String newName) {
        return new Product(id, newName, stock);
    }

    public Product withStock(int newStock) {
        return new Product(id, name, newStock);
    }
}
