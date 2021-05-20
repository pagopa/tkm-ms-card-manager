package it.gov.pagopa.tkm.ms.cardmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.*;

@SpringBootApplication
@EnableFeignClients
public class CardManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardManagerApplication.class, args);
	}

}
