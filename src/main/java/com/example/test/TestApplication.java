package com.example.test;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.uaa.clients.CreateClientRequest;
import org.cloudfoundry.uaa.clients.DeleteClientRequest;
import org.cloudfoundry.uaa.tokens.GrantType;
import org.cloudfoundry.util.ResourceUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.util.Logger;
import reactor.util.Loggers;

@SpringBootApplication
public class TestApplication implements CommandLineRunner {
	private final Logger log = Loggers.getLogger(TestApplication.class);

	private CloudFoundryOperations cloudFoundryOperations;
	private CloudFoundryClient cloudFoundryClient;
	private UaaClient uaaClient;

	public TestApplication(final CloudFoundryOperations cloudFoundryOperations,
						   final CloudFoundryClient cloudFoundryClient,
						   final UaaClient uaaClient) {
		this.cloudFoundryOperations = cloudFoundryOperations;
		this.cloudFoundryClient = cloudFoundryClient;
		this.uaaClient = uaaClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Override
	public void run(String... args) {
		this.uaaClient.clients().delete(DeleteClientRequest.builder()
				.clientId("test-client")
				.build())
				.then(this.uaaClient.clients().create(CreateClientRequest.builder()
						.clientId("test-client")
						.clientSecret("test-client-secret")
						.authorizedGrantTypes(GrantType.CLIENT_CREDENTIALS)
						.authorities("uaa.resource")
						.scopes("cloud_controller.read", "cloud_controller.write", "cloud_controller.admin", "openid")
						.build()))
				.block();

		ReactorCloudFoundryClient newClient = ReactorCloudFoundryClient.builder()
				.from((ReactorCloudFoundryClient) cloudFoundryClient)
				.tokenProvider(ClientCredentialsGrantTokenProvider.builder()
						.clientId("test-client")
						.clientSecret("test-client-secret")
						.build())
				.build();

		log.info("Listing organizations using client");

		newClient.organizations().list(ListOrganizationsRequest.builder()
				.build())
				.flatMapMany(ResourceUtils::getResources)
				.map(ResourceUtils::getId)
				.doOnNext(orgId -> log.info("found organization '{}'", orgId))
				.blockLast();

		log.info("Finished listing organizations using client");

		CloudFoundryOperations newOperations = DefaultCloudFoundryOperations.builder()
				.from((DefaultCloudFoundryOperations) cloudFoundryOperations)
				.cloudFoundryClient(newClient)
				.build();

		log.info("Listing organizations using operations");

		newOperations.organizations()
				.list()
				.map(OrganizationSummary::getName)
				.doOnNext(orgName -> log.info("found organization '{}'", orgName))
				.blockLast();

		log.info("Finished listing organizations using operations");
	}
}
