package electromart.ElectroMart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import electromart.ElectroMart.service.EmailService;

@RestController
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/api/test-email")
    public String testEmail() {

        emailService.sendEmail(
                "surajmrj12072005@gmail.com",   // <-- Apna Gmail yahan likho
                "ElectroMart",
                "Congratulations! Your Email Service is Working."
        );

        return "Email Sent Successfully!";
    }
}