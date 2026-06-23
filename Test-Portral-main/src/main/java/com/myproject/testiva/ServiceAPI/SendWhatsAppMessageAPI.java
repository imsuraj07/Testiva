package com.myproject.testiva.ServiceAPI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.TestInfo;

@Service
public class SendWhatsAppMessageAPI {
	
	private final String INSTANCE_ID = "instance123869";
	private final String TOKEN = "kty574xyt1v6tih3";
	
	private final String URL = "https://api.ultramsg.com/" + INSTANCE_ID + "/messages/chat";
	
	public String sendTestReminderMessage(StudentInfo studentInfo, TestInfo testInfo)
	{
		String message = "📢 *Test Reminder*\n\n" +
		           "Dear " + studentInfo.getName() + ",\n\n" +
		           "📝 Your test *\"" + testInfo.getTestName() + "\"*\n (Test ID: *" + testInfo.getTestId() + "*)\n" +
		           "for course: *" + testInfo.getCourse() + "*,\nbranch: *" + testInfo.getBranch() + "*, \nyear: *" + testInfo.getYear() + "*\n\n" +
		           "is scheduled to begin in *15 minutes*.\n\n" +
		           "🕒 Start Time: *" + testInfo.getStartTime().toLocalTime() + "*,\n" +
		           "📅 Date: *" + testInfo.getStartTime().toLocalDate() + "*\n" +
		           "❓ Total Questions: *" + testInfo.getNumberOfQuestions() + "*\n\n" +
		           "⏲️ Test Active Time: *" + testInfo.getTestDuration() + "*\n\n" +
		           
		           "*Important 🔔*\nLogin Through your creadentials and please give your test within the Active time by using *TestID : "+testInfo.getTestId()+"*\\n\n" +
		           
				   "*Login Creadentials :*" +
				   "User Id : "+studentInfo.getEmail()+
				   "Password : "+studentInfo.getPassword()+
		           "\n\n✅ Please be ready and ensure a stable internet connection.\n" +
		           "Good luck! 🍀\n\n" +
		           "-- Team Testiva 🧑‍💻⚙️";
		
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("token", TOKEN);
		map.add("to", studentInfo.getContactno());
		map.add("body", message);
		
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(URL, request, String.class);
		
		System.err.println("Message Sent to "+studentInfo.getContactno());
		return responseEntity.getBody();
	}
	
	
}
