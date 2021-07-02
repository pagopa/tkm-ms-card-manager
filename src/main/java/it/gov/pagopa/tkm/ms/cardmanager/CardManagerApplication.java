package it.gov.pagopa.tkm.ms.cardmanager;

import it.gov.pagopa.tkm.aop.AopLogging;
import it.gov.pagopa.tkm.config.CustomAnnotation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@Import({CustomAnnotation.class, AopLogging.class})
public class CardManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardManagerApplication.class, args);
    }

}
