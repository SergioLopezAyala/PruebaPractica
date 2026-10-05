package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto;

public record TopStockProductResponse(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock) {
}
