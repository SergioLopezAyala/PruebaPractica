package io.github.sergiolopezayala.franchise.domain.model;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record Franchise(String id, String name, Long version, List<Branch> branches) {

    public Franchise {
        name = Names.normalize(name);
        branches = branches == null ? List.of() : List.copyOf(branches);
    }

    public static Franchise create(String name) {
        return new Franchise(null, name, null, List.of());
    }

    public Franchise rename(String newName) {
        return new Franchise(id, newName, version, branches);
    }

    public Branch findBranch(String branchId) {
        return branches.stream()
                .filter(branch -> branch.id().equals(branchId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Branch", branchId));
    }

    public Franchise addBranch(Branch branch) {
        ensureUniqueBranchName(branch.name(), null);
        return withBranches(Stream.concat(branches.stream(), Stream.of(branch)).toList());
    }

    public Franchise renameBranch(String branchId, String newName) {
        Branch renamed = findBranch(branchId).rename(newName);
        ensureUniqueBranchName(renamed.name(), branchId);
        return updateBranch(branchId, branch -> renamed);
    }

    public Franchise addProduct(String branchId, Product product) {
        return updateBranch(branchId, branch -> branch.addProduct(product));
    }

    public Franchise removeProduct(String branchId, String productId) {
        return updateBranch(branchId, branch -> branch.removeProduct(productId));
    }

    public Franchise updateProductStock(String branchId, String productId, int stock) {
        return updateBranch(branchId, branch -> branch.updateProductStock(productId, stock));
    }

    public Franchise renameProduct(String branchId, String productId, String newName) {
        return updateBranch(branchId, branch -> branch.renameProduct(productId, newName));
    }

    public List<TopStockProduct> topStockPerBranch() {
        return branches.stream()
                .flatMap(branch -> branch.topStockProduct()
                        .map(product -> TopStockProduct.of(branch, product))
                        .stream())
                .toList();
    }

    private Franchise updateBranch(String branchId, UnaryOperator<Branch> change) {
        findBranch(branchId);
        return withBranches(branches.stream()
                .map(branch -> branch.id().equals(branchId) ? change.apply(branch) : branch)
                .toList());
    }

    private void ensureUniqueBranchName(String candidate, String ignoredBranchId) {
        boolean taken = branches.stream()
                .filter(branch -> !branch.id().equals(ignoredBranchId))
                .anyMatch(branch -> Names.sameName(branch.name(), candidate));
        if (taken) {
            throw new DuplicateNameException("Branch", candidate);
        }
    }

    private Franchise withBranches(List<Branch> newBranches) {
        return new Franchise(id, name, version, newBranches);
    }
}
