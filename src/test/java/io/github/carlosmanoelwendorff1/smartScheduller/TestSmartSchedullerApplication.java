package io.github.carlosmanoelwendorff1.smartScheduller;

import org.springframework.boot.SpringApplication;

public class TestSmartSchedullerApplication {

	public static void main(String[] args) {
		SpringApplication.from(SmartSchedullerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
