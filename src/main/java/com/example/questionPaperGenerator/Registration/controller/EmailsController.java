package com.example.questionPaperGenerator.Registration.controller;

import com.example.questionPaperGenerator.Registration.business.EmailAttachmentReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class EmailsController {
    @Autowired
    private EmailAttachmentReceiver emailAttachmentReceiver;

    @Scheduled(fixedDelay = 60000,initialDelay  = 10000)
    public void checkIncomingMail() {

        emailAttachmentReceiver.downloadEmailAttachments("outlook.office365.com","993","bluorange1@jagular.co.za","@gigi100");
    }

    @GetMapping("0o")
    public ResponseEntity<?> checkResponse() {


        return ResponseEntity.ok("checks");

    }








}
