package it.gov.pagopa.tkm.ms.cardmanager.service.impl;

import it.gov.pagopa.tkm.ms.cardmanager.exception.CardException;
import it.gov.pagopa.tkm.ms.cardmanager.service.MessageValidatorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ErrorCodeEnum.MESSAGE_VALIDATION_FAILED;

@Service
@Log4j2
public class MessageValidatorServiceImpl implements MessageValidatorService {
    @Autowired
    private Validator validator;

    public <T> void validateMessage(T message) throws CardException {
        Set<ConstraintViolation<T>> violations = validator.validate(message);
        if (!CollectionUtils.isEmpty(violations)) {
            log.error("Validation errors: " + violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; ")));
            throw new CardException(MESSAGE_VALIDATION_FAILED);
        }
    }
}
