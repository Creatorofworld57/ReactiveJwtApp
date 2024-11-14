package org.packet.reactivewebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "org.packet.reactivewebapp.Repositories")
@EnableWebFlux
public class ReactiveWebAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveWebAppApplication.class, args);
	}

}
