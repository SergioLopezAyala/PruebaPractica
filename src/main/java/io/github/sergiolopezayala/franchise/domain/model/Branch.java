package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record Branch(String id, String name, List<Product> products) {

    private static final Comparator<Product> MOST_STOCK_FIRST = Comparator
            .comparingInt(Product::stock).reversed()
            .thenComparing(Product::name, String.CASE_INSENSITIVE_ORDER);

    public Branch {
        name = Names.normalize(name);
        products = products == null ? List.of() : List.copyOf(products);
    }

    public static Branch create(String id, String name) {
        return new Branch(id, name, List.of());
    }

    public Branch rename(String newName) {
        return new Branch(id, newName, products);
    }

    public Product findProduct(String productId) {
        return products.stream()
                .filter(product -> product.id().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product", productId));
    }

    public Branch addProduct(Product product) {
        ensureUniqueName(product.name(), null);
        return withProducts(Stream.concat(products.stream(), Stream.of(product)).toList());
    }

    public Branch removeProduct(String productId) {
        findProduct(productId);
        return withProducts(products.stream()
                .filter(product -> !product.id().equals(productId))
                .toList());
    }

    public Branch updateProductStock(String productId, int stock) {
        return updateProduct(productId, product -> product.withStock(stock));
    }

    public Branch renameProduct(String productId, String newName) {
        Product renamed = findProduct(productId).rename(newName);
        ensureUniqueName(renamed.name(), productId);
        return updateProduct(productId, product -> renamed);
    }

    public Optional<Product> topStockProduct() {
        return products.stream().min(MOST_STOCK_FIRST);
    }

    private Branch updateProduct(String productId, UnaryOperator<Product> change) {
        findProduct(productId);
        return withProducts(products.stream()
                .map(product -> product.id().equals(productId) ? change.apply(product) : product)
                .toList());
    }

    private void ensureUniqueName(String candidate, String ignoredProductId) {
        boolean taken = products.stream()
                .filter(product -> !product.id().equals(ignoredProductId))
                .anyMatch(product -> Names.sameName(product.name(), candidate));
        if (taken) {
            throw new DuplicateNameException("Product", candidate);
        }
    }

    private Branch withProducts(List<Product> newProducts) {
        return new Branch(id, name, newProducts);
    }
}
