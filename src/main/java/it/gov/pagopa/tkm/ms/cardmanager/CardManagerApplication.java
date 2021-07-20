package it.gov.pagopa.tkm.ms.cardmanager;

import it.gov.pagopa.tkm.annotation.EnableTkmAopLogging;
import it.gov.pagopa.tkm.annotation.EnableTkmLoggingTableResult;
import it.gov.pagopa.tkm.annotation.EnableTkmStringAnnotation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableTkmAopLogging
@EnableTkmStringAnnotation
@EnableTkmLoggingTableResult
@EnableScheduling
public class CardManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardManagerApplication.class, args);
    }

}
