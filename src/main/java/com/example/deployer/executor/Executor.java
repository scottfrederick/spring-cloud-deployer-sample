package com.example.deployer.executor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public interface Executor {
	Flux<String> runInParallel(int start, int count, Function<Integer, Mono<String>> consumer);
	Flux<String> runInParallel(List<String> items, Function<String, Mono<String>> consumer);
}