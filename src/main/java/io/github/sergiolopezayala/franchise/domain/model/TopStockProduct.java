package io.github.sergiolopezayala.franchise.domain.model;

public record TopStockProduct(String branchId, String branchName, String productId, String productName, int stock) {

    static TopStockProduct of(Branch branch, Product product) {
        return new TopStockProduct(branch.id(), branch.name(), product.id(), product.name(), product.stock());
    }
}
