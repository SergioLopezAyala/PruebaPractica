package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchTest {

    private final Branch branch = new Branch("b1", "Downtown", List.of(
            new Product("p1", "Coffee", 10),
            new Product("p2", "Tea", 3)));

    @Test
    void createStartsWithoutProducts() {
        assertThat(Branch.create("b1", "Downtown").products()).isEmpty();
    }

    @Test
    void productsListIsImmutable() {
        assertThatThrownBy(() -> branch.products().add(new Product("p3", "Milk", 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void addsProduct() {
        Branch updated = branch.addProduct(new Product("p3", "Milk", 1));

        assertThat(updated.products()).extracting(Product::id).containsExactly("p1", "p2", "p3");
        assertThat(branch.products()).hasSize(2);
    }

    @Test
    void rejectsDuplicateProductNameIgnoringCase() {
        assertThatThrownBy(() -> branch.addProduct(new Product("p3", "coffee", 1)))
                .isInstanceOf(DuplicateNameException.class)
                .hasMessage("Product with name 'coffee' already exists");
    }

    @Test
    void removesProduct() {
        assertThat(branch.removeProduct("p1").products()).extracting(Product::id).containsExactly("p2");
    }

    @Test
    void removingMissingProductFails() {
        assertThatThrownBy(() -> branch.removeProduct("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product missing not found");
    }

    @Test
    void updatesProductStock() {
        assertThat(branch.updateProductStock("p2", 42).findProduct("p2").stock()).isEqualTo(42);
    }

    @Test
    void rejectsNegativeStockUpdate() {
        assertThatThrownBy(() -> branch.updateProductStock("p2", -5))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void updatingStockOfMissingProductFails() {
        assertThatThrownBy(() -> branch.updateProductStock("missing", 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void renamesProduct() {
        assertThat(branch.renameProduct("p2", " Green tea ").findProduct("p2").name()).isEqualTo("Green tea");
    }

    @Test
    void renamingProductToItsOwnNameIsAllowed() {
        assertThat(branch.renameProduct("p1", "COFFEE").findProduct("p1").name()).isEqualTo("COFFEE");
    }

    @Test
    void renamingProductToAnotherProductsNameFails() {
        assertThatThrownBy(() -> branch.renameProduct("p2", "Coffee"))
                .isInstanceOf(DuplicateNameException.class);
    }

    @Test
    void renamesBranch() {
        assertThat(branch.rename("Uptown").name()).isEqualTo("Uptown");
    }

    @Test
    void topStockProductIsTheOneWithMostStock() {
        assertThat(branch.topStockProduct()).contains(new Product("p1", "Coffee", 10));
    }

    @Test
    void topStockProductIsEmptyWithoutProducts() {
        assertThat(Branch.create("b2", "Empty").topStockProduct()).isEmpty();
    }

    @Test
    void topStockTieIsBrokenAlphabetically() {
        Branch tied = new Branch("b1", "Downtown", List.of(
                new Product("p1", "Tea", 10),
                new Product("p2", "apple juice", 10),
                new Product("p3", "Coffee", 10)));

        assertThat(tied.topStockProduct()).map(Product::name).contains("apple juice");
    }
}
