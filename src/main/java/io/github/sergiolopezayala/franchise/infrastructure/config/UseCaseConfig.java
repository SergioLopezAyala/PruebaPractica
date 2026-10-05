package io.github.sergiolopezayala.franchise.infrastructure.config;

import io.github.sergiolopezayala.franchise.application.usecase.AddBranchUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.AddProductUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.CreateFranchiseUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.DeleteProductUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.GetTopStockProductsUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.RenameBranchUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.RenameFranchiseUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.RenameProductUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.UpdateProductStockUseCase;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import io.github.sergiolopezayala.franchise.domain.port.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

    @Bean
    IdGenerator idGenerator() {
        return () -> UUID.randomUUID().toString();
    }

    @Bean
    CreateFranchiseUseCase createFranchiseUseCase(FranchiseRepositoryPort repository) {
        return new CreateFranchiseUseCase(repository);
    }

    @Bean
    RenameFranchiseUseCase renameFranchiseUseCase(FranchiseRepositoryPort repository) {
        return new RenameFranchiseUseCase(repository);
    }

    @Bean
    AddBranchUseCase addBranchUseCase(FranchiseRepositoryPort repository, IdGenerator idGenerator) {
        return new AddBranchUseCase(repository, idGenerator);
    }

    @Bean
    RenameBranchUseCase renameBranchUseCase(FranchiseRepositoryPort repository) {
        return new RenameBranchUseCase(repository);
    }

    @Bean
    AddProductUseCase addProductUseCase(FranchiseRepositoryPort repository, IdGenerator idGenerator) {
        return new AddProductUseCase(repository, idGenerator);
    }

    @Bean
    DeleteProductUseCase deleteProductUseCase(FranchiseRepositoryPort repository) {
        return new DeleteProductUseCase(repository);
    }

    @Bean
    UpdateProductStockUseCase updateProductStockUseCase(FranchiseRepositoryPort repository) {
        return new UpdateProductStockUseCase(repository);
    }

    @Bean
    RenameProductUseCase renameProductUseCase(FranchiseRepositoryPort repository) {
        return new RenameProductUseCase(repository);
    }

    @Bean
    GetTopStockProductsUseCase getTopStockProductsUseCase(FranchiseRepositoryPort repository) {
        return new GetTopStockProductsUseCase(repository);
    }
}
