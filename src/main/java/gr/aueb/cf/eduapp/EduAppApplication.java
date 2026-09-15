package gr.aueb.cf.eduapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class EduAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduAppApplication.class, args);
    }

}
