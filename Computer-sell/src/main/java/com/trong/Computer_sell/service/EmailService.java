package com.trong.Computer_sell.service;


import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    @Value("${spring.sendgrid.from-email:trongho.373664@gmail.com}")
    private String from;

    private final SendGrid sendGrid;


    @Value("${spring.sendgrid.template-id}")
    private String templateId;

    @Value("${spring.sendgrid.verification-link}")
    private String verificationLink;


    /**
     * send email by sendgrid
     * @param to
     * @param subject
     * @param text
     */
    public void send(String to, String subject, String text){
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        Content content = new Content("text/plain", text);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            if(response.getStatusCode() == 202){
                log.info("Email sent successfully");
            }else{
                log.error("Failed to send email");
            }
            log.info(response.getBody());
        } catch (IOException e) {
            log.error("Error occurred  to send email", e);
        }


    }

    /**
     * send email verification
     * @param to
     * @param name
     * @throws IOException
     */
    public void emailVerification(String to, String name) throws IOException {
        log.info("Send email to {}", to);
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        String subject = "Verify your email address";

        String secretCode = String.format("?secretCode=%s", UUID.randomUUID());
        //generate secret code and save to database


        //Dinh nghia Template

        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("verfication_link", verificationLink + secretCode);

        //Mail
        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);

        //Add to Dynamic Template Data
        map.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);
        mail.setTemplateId(templateId);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);

        if(response.getStatusCode() == 202){
            log.info("Email sent successfully");
        }else {
            log.error("Failed to send email");
        }

    }


}
