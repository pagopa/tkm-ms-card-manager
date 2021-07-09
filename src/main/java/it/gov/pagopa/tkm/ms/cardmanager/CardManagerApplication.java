package it.gov.pagopa.tkm.ms.cardmanager;

import it.gov.pagopa.tkm.aop.AopLogging;
import it.gov.pagopa.tkm.config.CustomAnnotation;
import it.gov.pagopa.tkm.config.*;
import it.gov.pagopa.tkm.service.*;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.openfeign.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@Import({CustomAnnotation.class, AopLogging.class})
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT6H")
public class CardManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardManagerApplication.class, args);
	}

}
