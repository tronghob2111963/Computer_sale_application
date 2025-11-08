package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.service.EmailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-CONTROLLER")
@Tag(name = "Email Controller")
public class EmailController {

    private final EmailService emailService;


    @GetMapping("/send")
    public ResponseData<Object> sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String text){
        try {
            log.info("Send email to {}", to);
            emailService.send(to, subject, text);
            return new ResponseData<>(HttpStatus.OK.value(), "Email sent successfully");
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/verifi-email")
    public ResponseData<Object> sendEmailVerfication(@RequestParam String to, String name){
        try {
            log.info("Send email to {}", to);
            emailService.emailVerification(to, name);
            return new ResponseData<>(HttpStatus.OK.value(), "Email sent successfully");
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
