package com.trong.Computer_sell.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vietqr")
@Getter
@Setter
public class VietQRConfig {
    
    private String bankId = "970422";        // MB Bank mặc định
    private String accountNo = "0123456789"; // Số tài khoản
    private String accountName = "CONG TY TNHH THCOMPUTER"; // Tên tài khoản
    private String template = "compact2";    // Template QR
}
