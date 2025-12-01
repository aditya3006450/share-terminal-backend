package com.example.share_terminal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private TemplateEngine templateEngine;

  public void sendPasswordSetupMail(String to, String link) {
    try {
      Context context = new Context();
      context.setVariable("link", link);

      String htmlContent = templateEngine.process("setup-password-email", context);
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

      mimeMessageHelper.setTo(to);
      mimeMessageHelper.setSubject("Share-Terminal password setup");
      mimeMessageHelper.setText(htmlContent, true);

      mailSender.send(mimeMessage);
    } catch (MessagingException e) {
      throw new RuntimeException("Error sending password setup email", e);
    }

  }

}
