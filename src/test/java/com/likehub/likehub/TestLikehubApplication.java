package com.likehub.likehub;

import org.springframework.boot.SpringApplication;

public class TestLikehubApplication {

	public static void main(String[] args) {
		SpringApplication.from(LikehubApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
