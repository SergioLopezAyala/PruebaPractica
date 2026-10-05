package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.Product;

import java.util.List;

final class Fixtures {

    static final String FRANCHISE_ID = "f1";
    static final String BRANCH_ID = "b1";
    static final String PRODUCT_ID = "p1";

    private Fixtures() {
    }

    static Franchise franchise() {
        return new Franchise(FRANCHISE_ID, "Acme", 1L, List.of(
                new Branch(BRANCH_ID, "Downtown", List.of(
                        new Product(PRODUCT_ID, "Coffee", 10),
                        new Product("p2", "Tea", 3)))));
    }
}
