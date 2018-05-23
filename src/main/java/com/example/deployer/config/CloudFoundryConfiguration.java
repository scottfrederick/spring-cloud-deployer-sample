package com.example.deployer.config;

import com.example.deployer.deployer.ReactiveAppDeployer;
import com.example.deployer.deployer.CloudFoundryReactiveAppDeployer;
import com.github.zafarkhaja.semver.Version;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.AppNameGenerator;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppNameGenerator;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryConnectionProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerAutoConfiguration;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeploymentProperties;
import org.springframework.cloud.deployer.spi.core.RuntimeEnvironmentInfo;
import org.springframework.cloud.deployer.spi.util.RuntimeVersionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@AutoConfigureBefore(CloudFoundryDeployerAutoConfiguration.class)
public class CloudFoundryConfiguration {
	@Autowired
	private CloudFoundryDeployerAutoConfiguration.EarlyConnectionConfiguration connectionConfiguration;

	@Bean
	@Primary
	@ConfigurationProperties(prefix = CloudFoundryConnectionProperties.CLOUDFOUNDRY_PROPERTIES)
	public CloudFoundryDeploymentProperties appDeploymentProperties() {
		return new CloudFoundryDeploymentProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = CloudFoundryConnectionProperties.CLOUDFOUNDRY_PROPERTIES)
	public CloudFoundryConnectionProperties cloudFoundryConnectionProperties() {
		return new CloudFoundryConnectionProperties();
	}

	@Bean
	public AppNameGenerator appNameGenerator() {
		return new CloudFoundryAppNameGenerator(appDeploymentProperties());
	}

	@Bean
	public ReactiveAppDeployer cloudFoundryAppDeployer(AppNameGenerator applicationNameGenerator,
													   CloudFoundryOperations operations,
													   CloudFoundryClient cloudFoundryClient) {
		return new CloudFoundryReactiveAppDeployer(applicationNameGenerator,
				appDeploymentProperties(),
				operations,
				runtimeEnvironmentInfo(cloudFoundryClient, ReactiveAppDeployer.class, CloudFoundryReactiveAppDeployer.class));
	}

	private RuntimeEnvironmentInfo runtimeEnvironmentInfo(CloudFoundryClient client,
														  Class spiClass,
														  Class implementationClass) {
		Version version = connectionConfiguration.version(client);
		return new RuntimeEnvironmentInfo.Builder()
				.implementationName(implementationClass.getSimpleName())
				.spiClass(spiClass)
				.implementationVersion(RuntimeVersionUtils.getVersion(CloudFoundryReactiveAppDeployer.class))
				.platformType("Cloud Foundry")
				.platformClientVersion(RuntimeVersionUtils.getVersion(client.getClass()))
				.platformApiVersion(version.toString())
				.platformHostVersion("unknown")
				.addPlatformSpecificInfo("API Endpoint", cloudFoundryConnectionProperties().getUrl().toString())
				.build();
	}

}
