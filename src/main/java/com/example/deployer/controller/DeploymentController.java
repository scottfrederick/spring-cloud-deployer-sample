package com.example.deployer.controller;

import com.example.deployer.deployer.ReactiveAppDeployer;
import com.example.deployer.executor.Executor;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/deployment")
public class DeploymentController {
	private ReactiveAppDeployer appDeployer;
	private Executor executor;

	private List<String> deployedAppIds = new ArrayList<>();

	public DeploymentController(ReactiveAppDeployer appDeployer, Executor executor) {
		this.appDeployer = appDeployer;
		this.executor = executor;
	}

	@PutMapping
	public Flux<String> deploy(@RequestParam(required = false, defaultValue = "1") int count) {
		return executor
				.runInParallel(deployedAppIds.size(), count, this::deployGreeting)
				.doOnNext(appId -> deployedAppIds.add(appId))
				.map(index -> "started deployment of app " + index + "\n");
	}

	@DeleteMapping
	public Flux<String> undeploy() {
		return executor
				.runInParallel(new ArrayList<>(deployedAppIds), this::undeployGreeting)
				.doOnNext(appId -> deployedAppIds.remove(appId))
				.map(index -> "started undeployment of app " + index + "\n");
	}

	@GetMapping
	public String status() {
		return deployedAppIds.stream()
				.sorted()
				.map(this::buildStatusString)
				.collect(Collectors.joining("\n"));
	}

	private Mono<String> deployGreeting(Integer index) {
		AppDefinition appDefinition = new AppDefinition("greeting" + index, null);
		ClassPathResource appResource = new ClassPathResource("/greeting.jar");
		AppDeploymentRequest request = new AppDeploymentRequest(appDefinition, appResource);

		return appDeployer.deploy(request);
	}

	private Mono<String> undeployGreeting(String appId) {
		return appDeployer.undeploy(appId);
	}

	private String buildStatusString(String appId) {
		return appId + ": " + appDeployer.status(appId);
	}
}
