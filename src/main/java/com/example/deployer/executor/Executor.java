package com.example.deployer.executor;

import java.util.List;
import java.util.function.Consumer;

public interface Executor {
	void runInParallel(int count, Consumer<Integer> consumer);
	void runInParallel(List<String> items, Consumer<String> consumer);
}