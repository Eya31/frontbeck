package tn.SGII_Ville;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class SgiiVilleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgiiVilleApplication.class, args);
    }

}
