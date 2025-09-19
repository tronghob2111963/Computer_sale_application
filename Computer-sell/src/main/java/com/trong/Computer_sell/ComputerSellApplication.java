package com.trong.Computer_sell;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComputerSellApplication {

	@Value("${jwt.secretKey}")
	private String jwtSecretKey;

	public static void main(String[] args) {
		SpringApplication.run(ComputerSellApplication.class, args);

	}
	@PostConstruct
	public void Test(){
		System.out.println(jwtSecretKey);
	}

}
