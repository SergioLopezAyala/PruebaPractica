package io.github.sergiolopezayala.franchise.infrastructure.persistence.mongo;

import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.infrastructure.persistence.mongo.FranchiseDocument.BranchDocument;
import io.github.sergiolopezayala.franchise.infrastructure.persistence.mongo.FranchiseDocument.ProductDocument;

import java.util.List;

final class FranchiseDocumentMapper {

    private FranchiseDocumentMapper() {
    }

    static FranchiseDocument toDocument(Franchise franchise) {
        return new FranchiseDocument(
                franchise.id(),
                franchise.name(),
                franchise.version(),
                franchise.branches().stream().map(FranchiseDocumentMapper::toDocument).toList());
    }

    static Franchise toDomain(FranchiseDocument document) {
        return new Franchise(
                document.id(),
                document.name(),
                document.version(),
                nullSafe(document.branches()).stream().map(FranchiseDocumentMapper::toDomain).toList());
    }

    private static BranchDocument toDocument(Branch branch) {
        return new BranchDocument(
                branch.id(),
                branch.name(),
                branch.products().stream()
                        .map(product -> new ProductDocument(product.id(), product.name(), product.stock()))
                        .toList());
    }

    private static Branch toDomain(BranchDocument document) {
        return new Branch(
                document.id(),
                document.name(),
                nullSafe(document.products()).stream()
                        .map(product -> new Product(product.id(), product.name(), product.stock()))
                        .toList());
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
