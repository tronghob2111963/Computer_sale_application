package com.trong.Computer_sell.DTO.response.common;

import org.springframework.http.HttpStatusCode;

public class ResponseValue extends ResponseScuccess {
    public ResponseValue(HttpStatusCode status, String message) {
        super(status, message);
    }
}