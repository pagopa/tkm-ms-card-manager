package it.gov.pagopa.tkm.ms.cardmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.config.ErrorHandler;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.controller.impl.KnownHashesControllerImpl;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TestKnownHashesController {

    @InjectMocks
    private KnownHashesControllerImpl knownHashController;

    @Mock
    private CardRepository cardRepository;

    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(knownHashController)
                .setMessageConverters(
                        new ByteArrayHttpMessageConverter(),
                        new StringHttpMessageConverter(),
                        new ResourceHttpMessageConverter(),
                        new FormHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(),
                        new Jaxb2RootElementHttpMessageConverter())
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void getKnownHashpanSet_onePage() throws Exception {
        when(cardRepository.findByHpanIsNotNull(Mockito.any())).thenReturn(CardRepositoryMock.getOnePageTkmCard());

        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_KNOWN_HASHES)
                        .queryParam(ApiParams.MAX_NUMBER_OF_RECORDS_PARAM, "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(Constant.HASH_1, Constant.HASH_2))));
    }

    @Test
    void getKnownHashpanSet_InvalidNumRecordsMin() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashpanSetMethod = KnownHashesController.class.getMethod("getKnownHashes", Integer.class, Integer.class, HttpServletResponse.class);
        Object[] parameterValues = {KnownHashesController.MIN_VALUE - 1, 0, null};
        Set<ConstraintViolation<KnownHashesControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashesControllerImpl(), getKnownHashpanSetMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

    @Test
    void getKnownHashpanSet_InvalidNumRecordsMax() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashpanSetMethod = KnownHashesController.class.getMethod("getKnownHashes", Integer.class, Integer.class, HttpServletResponse.class);
        Object[] parameterValues = {KnownHashesController.MAX_VALUE + 1, 0, null};
        Set<ConstraintViolation<KnownHashesControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashesControllerImpl(), getKnownHashpanSetMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

}
