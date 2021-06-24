package it.gov.pagopa.tkm.ms.cardmanager;

import it.gov.pagopa.tkm.config.*;
import it.gov.pagopa.tkm.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.context.annotation.*;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@Import({CustomAnnotation.class, PgpUtils.class})
public class CardManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardManagerApplication.class, args);
	}

}
