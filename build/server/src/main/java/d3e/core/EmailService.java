package d3e.core;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import classes.Env;
import models.EmailMessage;
import models.VerificationData;
import store.Database;

@Service
public class EmailService implements Runnable {
  @Autowired
  private Environment env;

  private LinkedBlockingQueue<EmailMessage> emails = new LinkedBlockingQueue<>();
  
  @Autowired
  private TransactionWrapper wrapper;

  @PostConstruct
  public void init() {
    new Thread(this).start();
  }

  public void send(EmailMessage mail) {
    if (mail == null) {
      return;
    }
    pushEmail(mail);
  }
  
  public void sendVerificationEmail(String email, String context, String body, boolean html, String subject) {
    if (email == null || context == null) {
      return;
    }
    /*
     * Generate token
     */
    String token = UUID.randomUUID().toString();
    
    /*
     * Save VerificationData object
     */
    VerificationData data = new VerificationData();
    data.setMethod(email);
    data.setContext(context);
    data.setToken(token);
    data.setBody(body);
    data.setSubject(subject);
    try {
      wrapper.doInTransaction(() -> {
        Database.get().save(data);
      });
    } catch (ServletException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    /*
     * Generate link
     */
    String link = Env.get().getBaseHttpUrl() + "/verify?code=" + token;
    
    /*
     * Send link via email
     */
    if (body.isEmpty()) {
      body = "This is your verification link: {link}";
    }
    body = StringExt.replaceAll(body, "\\{link\\}", link);
    if (subject.isEmpty()) {
      subject = "Verification link";
    }
    EmailMessage msg = new EmailMessage();
    msg.setTo(ListExt.asList(email));
    msg.setSubject(subject);
    msg.setBody(body);
    msg.setHtml(html);
    send(msg);
  }
  
  @Override
  public void run() {
    while (true) {
      EmailMessage mail = null;
      try {
        mail = emails.take();
        sendEmail(mail);
      } catch (MessagingException e) {
        if (mail != null) {
          e.printStackTrace(System.err);
        }
      } catch (InterruptedException e) {
      }
    }
  }

  private synchronized void pushEmail(EmailMessage mail) {
    emails.add(mail);
  }

  private String getEnvString(String str) {
    return EnvironmentHelper.getEnvString(env, str);
  }

  private void sendEmail(EmailMessage email) throws AddressException, MessagingException {
    Properties prop = new Properties();

    prop.put("mail.smtp.auth", true);
    prop.put("mail.smtp.starttls.enable", true);
    prop.put("mail.smtp.host", getEnvString("{env.mail.smtp.host}"));
    prop.put("mail.smtp.port", getEnvString("{env.mail.smtp.port}"));

    String username = getEnvString("{env.mail.uname}");
    String password = getEnvString("{env.mail.pwd}");
    email.setFrom(getEnvString("{env.mail.sender}"));

    Session session = Session.getInstance(prop, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    });
    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(email.getFrom()));
    message.setRecipients(RecipientType.TO, InternetAddress.parse(String.join(",", email.getTo())));
    message.setRecipients(RecipientType.CC, InternetAddress.parse(String.join(",", email.getCc())));
    message.setRecipients(RecipientType.BCC, InternetAddress.parse(String.join(",", email.getBcc())));
    message.setSubject(email.getSubject());
    if(email.isHtml()) {
    	message.setContent(email.getBody(), "text/html");
    } else {
    	message.setText(email.getBody());
    }
    Transport.send(message);
  }
}
