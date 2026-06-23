package com.myproject.testiva.ServiceAPI;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.StudentInfo.UserRole;
import com.myproject.testiva.Model.TestInfo;
import com.myproject.testiva.Model.TestInfo.TestStatus;
import com.myproject.testiva.Repository.StudentInfoRepo;
import com.myproject.testiva.Repository.TestInfoRepo;

@Component
public class TestSchedular {
	
	@Autowired
	private TestInfoRepo testInfoRepo;
	
	@Autowired
	private StudentInfoRepo studentInfoRepo;
	
	@Autowired
	private SendWhatsAppMessageAPI whatsAppMessageAPI;
	
	@Scheduled(fixedRate = 30000)
	public void manageScheduledTest()
	{
		ZonedDateTime zoneTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
		LocalDateTime currentTime = zoneTime.toLocalDateTime();
		
		List<TestInfo> testList = testInfoRepo.findAll();
		
		for(TestInfo test : testList)
		{
			LocalDateTime startTime = test.getStartTime();
			LocalDateTime endTime = test.endtime();
			
			//1. When Test Status Scheduled: send reminder 15 mins before
			if(test.getStatus().equals(TestStatus.Scheduled) && currentTime.isAfter(startTime.minusMinutes(15)) && currentTime.isBefore(startTime.minusMinutes(10)))
			{
				//send reminder
				List<StudentInfo> stdList = studentInfoRepo.findAllByRoleAndCourseAndBranchAndYear(UserRole.STUDENT, test.getCourse(), test.getBranch(), test.getYear());
				
				for(StudentInfo student : stdList)
				{
					try {
						// whatsAppMessageAPI.sendTestReminderMessage(student, test); // Disabled due to expired API subscription
					} catch (Exception e) {
						System.err.println("WhatsApp notification failed: " + e.getMessage());
					}
				}
				
				test.setStatus(TestStatus.Reminder_Sent);
				testInfoRepo.save(test);
			}
			
			//2. Activate test 10 min before startTime (aligns with student's early access window)
			boolean isPreActive = test.getStatus().equals(TestStatus.Reminder_Sent) || test.getStatus().equals(TestStatus.Scheduled);
			if(isPreActive && currentTime.isAfter(startTime.minusMinutes(10)))
			{
				test.setStatus(TestStatus.Active);
				testInfoRepo.save(test);
			}
			
			//3. When Test is Active and time is over, end the test
			if (test.getStatus().equals(TestStatus.Active) && currentTime.isAfter(endTime)) {
				test.setStatus(TestStatus.Test_Over);
				testInfoRepo.save(test);
			}
			
		}
	}
}
