package com.example.questionPaperGenerator.emailModule;

import com.example.questionPaperGenerator.dto.Mail;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
public class EmailServices {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private Configuration configuration;

    public void sendEmail(Mail mail,FileSystemResource file) throws IOException, TemplateException {

        try {

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name());


            Template template = configuration.getTemplate("emailTemplate.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());

           // mimeMessageHelper.setTo(mail.getTo());
            mimeMessageHelper.setTo(InternetAddress.parse("rmaphori@gmail.com, remz@jagular.co.za, "));
            mimeMessageHelper.setText(html, true);
            mimeMessageHelper.setSubject(mail.getSubject());
            mimeMessageHelper.setFrom(mail.getFrom());

            //FileSystemResource file = new FileSystemResource("C:\\log.txt");
            //helper.addAttachment(file.getFilename(), file);

            mimeMessageHelper.addAttachment(file.getFilename(),file.getFile());

            javaMailSender.send(message);
            boolean isDeleted =  file.getFile().delete();
            System.out.println(isDeleted +" is file deleted");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


    }
}
