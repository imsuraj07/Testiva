package com.myproject.testiva.ServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SendAutoEmail {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	public void sendEnquryResponseMail(com.myproject.testiva.Model.Enquiry enquiry, String subject, String message)
	{
		String msgText = "<!DOCTYPE html>"
				+ "<html>"
				+ "<head>"
				+ "    <meta charset=\"UTF-8\">"
				+ "    <title>Response to Your Enquiry</title>"
				+ "</head>"
				+ "<body style=\"font-family: Arial, sans-serif; background-color: #f2f4f6; margin: 0; padding:10px 20px;\">"
				+ "    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">"
				+ "        <tr>"
				+ "            <td align=\"center\">"
				+ "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); overflow: hidden;\">"
				+ "                    <!-- Header -->"
				+ "                    <tr style=\"background-color: #0d6efd;\">"
				+ "                        <td style=\"padding: 20px; text-align: center; color: #ffffff;\">"
				+ "                            <h2 style=\"margin: 0;\">Thank You for Contacting <span style=\"color: #ffc107;\">Testiva</span></h2>"
				+ "                        </td>"
				+ "                    </tr>"
				+ "                    <!-- Body -->"
				+ "                    <tr>"
				+ "                        <td style=\"padding: 30px;\">"
				+ "                            <p style=\"font-size: 16px; margin-bottom: 10px;\">Hi <strong>"+enquiry.getName()+"</strong>,</p>"
				+ "                            <p style=\"font-size: 15px; color: #333333;\">\r\n"
				+ "                                We have received your enquiry and appreciate you taking the time to reach out to us. Below is our response:"
				+ "                            </p>"
				+ "                            <div style=\"background-color: #f9f9f9; padding: 20px; margin: 20px 0; border-left: 4px solid #0d6efd;\">"
				+ "                                <p style=\"font-size: 15px; color: #333;\"><strong>Your Enquiry:</strong><br>"+enquiry.getEnquiryText()+"</p>"
				+ "                                <p style=\"font-size: 15px; color: #333;\"><strong>Our Response:</strong><br>"+message+"</p>"
				+ "                            </div>"
				+ "                            <p style=\"font-size: 15px; color: #333;\">"
				+ "                                If you have any further questions, feel free to reply to this email. We're here to help!"
				+ "                            </p>"
				+ "                            <p style=\"font-size: 15px; color: #333;\">Warm regards,</p>"
				+ "                            <p style=\"font-size: 15px; font-weight: bold; color: #0d6efd;\">Team Testiva</p>"
				+ "                        </td>"
				+ "                    </tr>"
				+ "                    <!-- Footer -->"
				+ "                    <tr style=\"background-color: #f1f1f1;\">"
				+ "                        <td style=\"padding: 20px; text-align: center; font-size: 13px; color: #777;\">"
				+ "                            © 2025 Testiva. All rights reserved.<br>"
				+ "                            <a href=\"https://www.testiva.com\" style=\"color: #0d6efd; text-decoration: none;\">www.testiva.com</a>"
				+ "                        </td>"
				+ "                    </tr>"
				+ "                </table>"
				+ "            </td>"
				+ "        </tr>"
				+ "    </table>"
				+ "</body>"
				+ "</html>"
				+ "";
		
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setTo(enquiry.getEmail());
			helper.setSubject(subject);
			helper.setText(msgText, true);
			javaMailSender.send(mimeMessage);
			System.err.println("Email Sent to "+enquiry.getEmail());
			
		} catch (Exception e) {
			System.err.println("Error : "+e.getMessage());
		}
		
	}
	
	
	public void sendVerificationStatusMail(com.myproject.testiva.Model.StudentInfo studentInfo)
	{
		String subject = "✅ Your Registration is Verified – You Can Now Log In!";
		String message =
	            "<html>" +
	            "<body style='font-family:Segoe UI, sans-serif; background-color:#f4f6f9; padding:20px;'>" +
	                "<div style='max-width:600px; margin:auto; background-color:white; border-radius:10px; " +
	                    "box-shadow:0 0 15px rgba(0,0,0,0.05); overflow:hidden;'>" +

	                    "<div style='background-color:#0d6efd; padding:20px; color:white; text-align:center;'>" +
	                        "<h2>🎉 Registration Verified!</h2>" +
	                    "</div>" +

	                    "<div style='padding:30px;'>" +
	                        "<p>Hi <strong>" + studentInfo.getName() + "</strong>,</p>" +
	                        "<p>We’re excited to let you know that your student registration has been " +
	                        "<strong style='color:green;'>successfully verified</strong> ✅ by the admin. 🎓</p>" +

	                        "<p>You can now log in to your student dashboard using your registered credentials.</p>" +

	                        "<p style='margin:20px 0;'>" +
	                            "<a href='https://yourdomain.com/student/login' " +
	                            "style='background-color:#0d6efd; color:white; padding:12px 24px; border-radius:5px; " +
	                            "text-decoration:none;'>🔐 Login Now</a>" +
	                        "</p>" +

	                        "<p>If you have any questions, feel free to reach out to us anytime 📬.</p>" +

	                        "<p>Warm regards,<br><strong>The Support Team</strong><br>9876543210<br>📚 EduPortal</p>" +
	                    "</div>" +

	                    "<div style='background-color:#f0f0f0; padding:15px; text-align:center; font-size:13px; color:#666;'>" +
	                        "© 2025 EduPortal. All rights reserved." +
	                    "</div>" +

	                "</div>" +
	            "</body>" +
	            "</html>";
		
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
			helper.setTo(studentInfo.getEmail());
			helper.setSubject(subject);
			helper.setText(message, true);
			javaMailSender.send(mimeMessage);
			System.err.println("Confirmation mail sent to "+studentInfo.getEmail());
		} catch (MessagingException e) {
			System.err.println("Something Went Wrong!!! \n"+e.getMessage());
		}
	}
}
