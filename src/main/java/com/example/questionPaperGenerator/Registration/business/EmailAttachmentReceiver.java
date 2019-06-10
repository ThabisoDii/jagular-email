package com.example.questionPaperGenerator.Registration.business;

import com.example.questionPaperGenerator.dto.Mail;
import com.example.questionPaperGenerator.emailModule.EmailServices;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;


@Component
public class EmailAttachmentReceiver {

    @Autowired
    private EmailServices emailServices;
    File[] attachedFiles;
    private String attachedFileName = "";

    private static String saveDirectory = "src\\main\\downloads"; // directory to save the downloaded documents

    /**
     * Sets the directory where attached files will be stored.
     * @param dir absolute path of the directory
     */
    public void setSaveDirectory(String dir) {
        EmailAttachmentReceiver.saveDirectory = dir;
    }

    /**
     * Downloads new messages and saves attachments to disk if any.
     */
    public synchronized void downloadEmailAttachments(String host, String port, String userName, String password) {
        Properties properties = new Properties();
        System.out.println("checking messages received");
        // server setting
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);

        // SSL setting
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port", String.valueOf(port));


            Session session = Session.getDefaultInstance(properties);

            try {
                // connects to the message store
                Store store = session.getStore("imap");
                store.connect(userName, password);

                // opens the inbox folder
                Folder folderInbox = store.getFolder("INBOX");
                folderInbox.open(Folder.READ_WRITE);

                Flags seen = new Flags(Flags.Flag.SEEN);
                FlagTerm unseenFlagTerm = new FlagTerm(seen,false);
                Message[] arrayMessages = folderInbox.search(unseenFlagTerm);

                System.out.println(" Unread messages count"+folderInbox.getUnreadMessageCount());

                if(arrayMessages.length ==0){
                    System.out.println(" Unread messages in array "+arrayMessages.length);
                }

                for (int i = 0; i < arrayMessages.length; i++) {

                    System.out.println("IS SEEN  :"+arrayMessages[i].isSet(Flags.Flag.SEEN));
                    Message message = arrayMessages[i];
                    arrayMessages[i].setFlag(Flags.Flag.SEEN,true);
                    Address[] fromAddress = message.getFrom();
                    String from = fromAddress[0].toString();
                    String subject = message.getSubject();
                    String sentDate = message.getSentDate().toString();


                    String contentType = message.getContentType();

                    String messageContent = "";

                    // store attachment file name, separated by comma
                    String attachFiles = "";

                    if (contentType.contains("multipart")) {
                        // content may contain attachments
                        Multipart multiPart = (Multipart) message.getContent();
                        int numberOfParts = multiPart.getCount();
                        for (int partCount = 0; partCount < numberOfParts; partCount++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                // this part is attachment
                                String fileName = part.getFileName();

                                attachFiles += fileName + ", ";
                                part.saveFile(saveDirectory + File.separator + fileName);
                                attachedFileName = fileName;
                            } else {
                                // this part may be the message content
                                messageContent = part.getContent().toString();
                                System.out.println("text "+messageContent);
                            }
                        }

                        if (attachFiles.length() > 1) {
                            attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                        }
                    } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    }



                    //print out details of each message
                    System.out.println("Message #" + (i + 1) + ":");
                    System.out.println("\t From: " + from);
                    System.out.println("\t Subject: " + subject);
                    System.out.println("\t Sent Date: " + sentDate);
                    System.out.println("\t Message: " + message.getContent().toString());
                    System.out.println("\t Attachments: " + attachFiles);

                    System.out.println("File name "+attachedFileName);

                    //saveDirectory
                   FileSystemResource fileSystemResource = new FileSystemResource(saveDirectory+"\\"+attachedFileName);
                    try {
                        sendEmail(fileSystemResource,messageContent);
                    } catch (TemplateException e) {
                        e.printStackTrace();
                    }
                }

                // disconnect
                folderInbox.close(false);
                store.close();

            } catch (NoSuchProviderException ex) {
                System.out.println("No provider for imap.");
                ex.printStackTrace();

            } catch (MessagingException ex) {
                System.out.println("Could not connect to the message store");
                ex.printStackTrace();

            } catch (IOException ex) {
                ex.printStackTrace();

            }

    }



    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }



    public void sendEmail(FileSystemResource fileSystemResource,String message) throws IOException, TemplateException {
        Mail mail = new Mail();
        Map<String,Object> model = new HashMap<>();
       // File file = new ClassPathResource(saveDirectory).getFile();
        //model.put("image",file);
        model.put("message",message);
        mail.setModel(model);
        mail.setFrom("bluorange@jagular.co.za");
        //mail.setTo("rmaphori@gmail.com, remz@jagular.co.za");
        mail.setSubject("bluorange");


        emailServices.sendEmail(mail,fileSystemResource);
    }
}
