package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto;

import java.util.List;

public record FranchiseResponse(String id, String name, List<BranchResponse> branches) {
}
