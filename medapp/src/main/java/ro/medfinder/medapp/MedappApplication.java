package ro.medfinder.medapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedappApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedappApplication.class, args);
	}

}
