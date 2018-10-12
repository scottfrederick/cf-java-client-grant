package com.example.test.config;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties
public class CloudFoundryJavaClientConfig {
	private static final String PROPERTY_PREFIX = "cloudfoundry";

	@Bean
	@ConfigurationProperties(PROPERTY_PREFIX)
	CloudFoundryProperties cloudFoundryTargetProperties() {
		return new CloudFoundryProperties();
	}

	@Bean
	ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorCloudFoundryClient.builder()
				.connectionContext(connectionContext)
				.tokenProvider(tokenProvider)
				.build();
	}

	@Bean
	CloudFoundryOperations cloudFoundryOperations(CloudFoundryProperties properties, CloudFoundryClient client,
												  DopplerClient dopplerClient, UaaClient uaaClient) {
		return DefaultCloudFoundryOperations.builder()
				.cloudFoundryClient(client)
				.dopplerClient(dopplerClient)
				.uaaClient(uaaClient)
				.organization(properties.getDefaultOrg())
				.space(properties.getDefaultSpace())
				.build();
	}

	@Bean
	DefaultConnectionContext connectionContext(CloudFoundryProperties properties) {
		return DefaultConnectionContext.builder()
				.apiHost(properties.getApiHost())
				.port(Optional.ofNullable(properties.getApiPort()))
				.skipSslValidation(properties.isSkipSslValidation())
				.secure(properties.isSecure())
				.build();
	}

	@Bean
	ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorDopplerClient.builder()
				.connectionContext(connectionContext)
				.tokenProvider(tokenProvider)
				.build();
	}

	@Bean
	ClientCredentialsGrantTokenProvider clientGrantTokenProvider(CloudFoundryProperties properties) {
		return ClientCredentialsGrantTokenProvider.builder()
				.clientId(properties.getClientId())
				.clientSecret(properties.getClientSecret())
				.identityZoneSubdomain(properties.getIdentityZoneSubdomain())
				.build();
	}

	@Bean
	ReactorUaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorUaaClient.builder()
				.connectionContext(connectionContext)
				.tokenProvider(tokenProvider)
				.build();
	}
}
