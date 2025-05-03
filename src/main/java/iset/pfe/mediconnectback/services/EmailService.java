package iset.pfe.mediconnectback.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.dtos.MailBody;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender mailSender;

	
	@Async
	public void sendEmail(MailBody mailBody) {
	
        SimpleMailMessage message = new SimpleMailMessage();
        
        message.setTo(mailBody.getTo());
        message.setSubject(mailBody.getSubject());
        message.setText(mailBody.getText());
        mailSender.send(message);
	}
	
}
