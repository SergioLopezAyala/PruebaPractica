package io.github.sergiolopezayala.franchise.infrastructure.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "franchises")
public record FranchiseDocument(
        @Id String id,
        @Indexed(unique = true, collation = FranchiseDocument.CASE_INSENSITIVE) String name,
        @Version Long version,
        List<BranchDocument> branches) {

    static final String CASE_INSENSITIVE = "{ 'locale': 'en', 'strength': 2 }";

    public record BranchDocument(String id, String name, List<ProductDocument> products) {
    }

    public record ProductDocument(String id, String name, int stock) {
    }
}
