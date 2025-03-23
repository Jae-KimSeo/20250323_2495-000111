package org.service.alarmfront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlarmFrontApplication {

public static void main(String[] args) {
		SpringApplication.run(AlarmFrontApplication.class, args);
	}

}
