package com.trong.Computer_sell.DTO.response;

import org.springframework.http.HttpStatusCode;

public class ResponseValue extends ResponseScuccess {
    public ResponseValue(HttpStatusCode status, String message) {
        super(status, message);
    }
}
