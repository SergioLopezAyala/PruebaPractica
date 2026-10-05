package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranchiseTest {

    private final Franchise franchise = new Franchise("f1", "Acme", 0L, List.of(
            new Branch("b1", "Downtown", List.of(new Product("p1", "Coffee", 10))),
            new Branch("b2", "Uptown", List.of())));

    @Test
    void createStartsWithoutIdVersionOrBranches() {
        Franchise created = Franchise.create(" Acme ");

        assertThat(created.id()).isNull();
        assertThat(created.version()).isNull();
        assertThat(created.name()).isEqualTo("Acme");
        assertThat(created.branches()).isEmpty();
    }

    @Test
    void renameKeepsIdVersionAndBranches() {
        Franchise renamed = franchise.rename("Globex");

        assertThat(renamed.name()).isEqualTo("Globex");
        assertThat(renamed.id()).isEqualTo("f1");
        assertThat(renamed.version()).isZero();
        assertThat(renamed.branches()).isEqualTo(franchise.branches());
    }

    @Test
    void addsBranch() {
        Franchise updated = franchise.addBranch(Branch.create("b3", "Airport"));

        assertThat(updated.branches()).extracting(Branch::id).containsExactly("b1", "b2", "b3");
    }

    @Test
    void rejectsDuplicateBranchName() {
        assertThatThrownBy(() -> franchise.addBranch(Branch.create("b3", "downtown")))
                .isInstanceOf(DuplicateNameException.class)
                .hasMessage("Branch with name 'downtown' already exists");
    }

    @Test
    void findingMissingBranchFails() {
        assertThatThrownBy(() -> franchise.findBranch("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Branch missing not found");
    }

    @Test
    void renamesBranch() {
        assertThat(franchise.renameBranch("b2", "Airport").findBranch("b2").name()).isEqualTo("Airport");
    }

    @Test
    void renamingBranchToAnotherBranchesNameFails() {
        assertThatThrownBy(() -> franchise.renameBranch("b2", "Downtown"))
                .isInstanceOf(DuplicateNameException.class);
    }

    @Test
    void renamingMissingBranchFails() {
        assertThatThrownBy(() -> franchise.renameBranch("missing", "Airport"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addsProductToBranch() {
        Franchise updated = franchise.addProduct("b2", new Product("p2", "Tea", 4));

        assertThat(updated.findBranch("b2").products()).containsExactly(new Product("p2", "Tea", 4));
        assertThat(updated.findBranch("b1")).isEqualTo(franchise.findBranch("b1"));
    }

    @Test
    void addingProductToMissingBranchFails() {
        assertThatThrownBy(() -> franchise.addProduct("missing", new Product("p2", "Tea", 4)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Branch missing not found");
    }

    @Test
    void removesProduct() {
        assertThat(franchise.removeProduct("b1", "p1").findBranch("b1").products()).isEmpty();
    }

    @Test
    void updatesProductStock() {
        assertThat(franchise.updateProductStock("b1", "p1", 99).findBranch("b1").findProduct("p1").stock())
                .isEqualTo(99);
    }

    @Test
    void renamesProduct() {
        assertThat(franchise.renameProduct("b1", "p1", "Espresso").findBranch("b1").findProduct("p1").name())
                .isEqualTo("Espresso");
    }

    @Test
    void topStockReturnsOneProductPerBranchAcrossMultipleBranches() {
        Franchise multi = new Franchise("f1", "Acme", 0L, List.of(
                new Branch("b1", "Downtown", List.of(
                        new Product("p1", "Coffee", 10),
                        new Product("p2", "Tea", 30))),
                new Branch("b2", "Uptown", List.of(
                        new Product("p3", "Milk", 7),
                        new Product("p4", "Juice", 2)))));

        assertThat(multi.topStockPerBranch()).containsExactly(
                new TopStockProduct("b1", "Downtown", "p2", "Tea", 30),
                new TopStockProduct("b2", "Uptown", "p3", "Milk", 7));
    }

    @Test
    void topStockOmitsBranchesWithoutProducts() {
        assertThat(franchise.topStockPerBranch()).containsExactly(
                new TopStockProduct("b1", "Downtown", "p1", "Coffee", 10));
    }

    @Test
    void topStockTieIsBrokenAlphabetically() {
        Franchise tied = new Franchise("f1", "Acme", 0L, List.of(
                new Branch("b1", "Downtown", List.of(
                        new Product("p1", "Tea", 10),
                        new Product("p2", "Coffee", 10)))));

        assertThat(tied.topStockPerBranch()).containsExactly(
                new TopStockProduct("b1", "Downtown", "p2", "Coffee", 10));
    }

    @Test
    void topStockIsEmptyWithoutBranches() {
        assertThat(Franchise.create("Acme").topStockPerBranch()).isEmpty();
    }
}
