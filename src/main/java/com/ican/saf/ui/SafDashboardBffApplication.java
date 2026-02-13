package com.ican.saf.ui;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class SafDashboardBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafDashboardBffApplication.class, args);
    }

}
