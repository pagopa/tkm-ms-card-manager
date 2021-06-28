package it.gov.pagopa.tkm.ms.cardmanager;

import it.gov.pagopa.tkm.config.*;
import it.gov.pagopa.tkm.service.*;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@Import({CustomAnnotation.class, PgpUtils.class})
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT6H")
public class CardManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardManagerApplication.class, args);
	}

}
