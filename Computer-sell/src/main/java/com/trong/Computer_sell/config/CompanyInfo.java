package com.trong.Computer_sell.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CompanyInfo {

    @Value("${seller.name}")
    private String name;

    @Value("${seller.tax}")
    private String tax;

    @Value("${seller.address}")
    private String address;

    @Value("${seller.phone}")
    private String phone;
}