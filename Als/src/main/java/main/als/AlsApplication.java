package main.als;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlsApplication.class, args);
    }

}
