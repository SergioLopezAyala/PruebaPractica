package io.github.sergiolopezayala.franchise.domain.port;

@FunctionalInterface
public interface IdGenerator {

    String newId();
}
