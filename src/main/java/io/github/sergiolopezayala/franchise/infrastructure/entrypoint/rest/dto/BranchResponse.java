package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto;

import java.util.List;

public record BranchResponse(String id, String name, List<ProductResponse> products) {
}
