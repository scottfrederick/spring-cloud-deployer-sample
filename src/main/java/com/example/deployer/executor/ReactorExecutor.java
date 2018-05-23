package com.example.deployer.executor;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Function;

@Component
public class ReactorExecutor implements Executor {
	public Flux<String> runInParallel(int start, int count, Function<Integer, Mono<String>> consumer) {
		return Flux.range(start, count)
				.subscribeOn(Schedulers.parallel())
				.flatMap(consumer);
	}

	public Flux<String> runInParallel(List<String> items, Function<String, Mono<String>> consumer) {
		return Flux.range(1, items.size())
				.subscribeOn(Schedulers.parallel())
				.map(index -> items.get(index - 1))
				.flatMap(consumer);
	}
}
