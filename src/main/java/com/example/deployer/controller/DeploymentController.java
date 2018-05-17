package com.example.deployer.controller;

import com.example.deployer.executor.Executor;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/deployment")
public class DeploymentController {
	private AppDeployer appDeployer;
	private Executor executor;

	private List<String> deployedAppIds = new ArrayList<>();

	public DeploymentController(AppDeployer appDeployer, Executor executor) {
		this.appDeployer = appDeployer;
		this.executor = executor;
	}

	@PutMapping
	public String deploy(@RequestParam(required = false, defaultValue = "1") int count) {
		executor.runInParallel(count, this::deployGreeting);
		return "started deployment of " + count + " apps";
	}

	@DeleteMapping
	public String undeploy() {
		executor.runInParallel(deployedAppIds, this::undeployGreeting);
		return "started undeployment of " + deployedAppIds.size() + " apps";
	}

	@GetMapping
	public String status() {
		return deployedAppIds.stream()
				.sorted()
				.map(this::buildStatusString)
				.collect(Collectors.joining("\n"));
	}

	private void deployGreeting(Integer index) {
		int offset = deployedAppIds.size() + index;

		AppDefinition appDefinition = new AppDefinition("greeting" + offset, null);
		ClassPathResource appResource = new ClassPathResource("/greeting.jar");
		AppDeploymentRequest request = new AppDeploymentRequest(appDefinition, appResource);
		String appId = appDeployer.deploy(request);

		deployedAppIds.add(appId);
	}

	private void undeployGreeting(String appId) {
		appDeployer.undeploy(appId);

		deployedAppIds.remove(appId);
	}

	private String buildStatusString(String appId) {
		return appId + ": " + appDeployer.status(appId);
	}
}
