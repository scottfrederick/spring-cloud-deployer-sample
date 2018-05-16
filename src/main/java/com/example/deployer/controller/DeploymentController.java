package com.example.deployer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

@RestController
@RequestMapping("/deployment")
public class DeploymentController {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);

	private AppDeployer appDeployer;

	public DeploymentController(AppDeployer appDeployer) {
		this.appDeployer = appDeployer;
	}

	@PutMapping
	public String deploy(@RequestParam(required = false, defaultValue = "1") int count) {
		runInParallel(count, this::deployGreeting);
		return "started deployment of " + count + " apps";
	}

	@DeleteMapping
	public String undeploy(@RequestParam(required = false, defaultValue = "1") int count) {
		runInParallel(count, this::undeployGreeting);
		return "started undeployment of " + count + " apps";
	}

	private void runInParallel(int count, Consumer<Integer> consumer) {
		Flux.range(1, count)
				.parallel(count)
				.runOn(Schedulers.parallel())
				.doOnTerminate(() -> logger.info("complete"))
				.subscribe(consumer);
	}

	private void deployGreeting(Integer index) {
		AppDefinition appDefinition = new AppDefinition("greeting" + index, null);
		ClassPathResource appResource = new ClassPathResource("/greeting.jar");
		AppDeploymentRequest request = new AppDeploymentRequest(appDefinition, appResource);
		appDeployer.deploy(request);
	}

	private void undeployGreeting(Integer index) {
		appDeployer.undeploy("greeting" + index);
	}
}
