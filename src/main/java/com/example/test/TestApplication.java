package com.example.test;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
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

	public TestApplication(final CloudFoundryOperations cloudFoundryOperations,
						   final CloudFoundryClient cloudFoundryClient) {
		this.cloudFoundryOperations = cloudFoundryOperations;
		this.cloudFoundryClient = cloudFoundryClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("Listing organizations using client");
		
		this.cloudFoundryClient.organizations().list(ListOrganizationsRequest.builder()
				.build())
				.flatMapMany(ResourceUtils::getResources)
				.map(ResourceUtils::getId)
				.doOnNext(orgId -> log.info("found organization '{}'", orgId))
				.blockLast();

		log.info("Finished listing organizations using client");

		log.info("Listing organizations using operations");

		this.cloudFoundryOperations.organizations()
				.list()
				.map(OrganizationSummary::getName)
				.doOnNext(orgName -> log.info("found organization '{}'", orgName))
				.blockLast();

		log.info("Finished listing organizations using operations");
	}
}
