package com.example.deployer.executor;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Consumer;

@Component
public class ReactorExecutor implements Executor {
	public void runInParallel(int count, Consumer<Integer> consumer) {
		Flux.range(1, count)
				.parallel(count)
				.runOn(Schedulers.parallel())
				.subscribe(consumer);
	}

	public void runInParallel(List<String> items, Consumer<String> consumer) {
		Flux.range(1, items.size())
				.parallel(items.size())
				.runOn(Schedulers.parallel())
				.map(index -> items.get(index - 1))
				.subscribe(consumer);
	}
}
