package com.trong.Computer_sell.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "\t\n" +
                        "Computer_sale",
                "api_key", "249267783779862",
                "api_secret", "QHtiWlZZ4RK9pLXh5EHgb_8OWFQ",
                "secure", true
        ));
    }
}