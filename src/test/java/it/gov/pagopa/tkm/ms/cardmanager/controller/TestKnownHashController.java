package it.gov.pagopa.tkm.ms.cardmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.config.ErrorHandler;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.controller.impl.KnownHashControllerImpl;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
import org.junit.jupiter.api.Assertions;
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

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.TOTAL_NUMBER_PAGES_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TestKnownHashController {
    @InjectMocks
    private KnownHashControllerImpl knownHashController;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardTokenRepository cardTokenRepository;

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
                get(ApiEndpoints.BASE_PATH_KNOWN_HASH + "/pan")
                        .queryParam(ApiParams.MAX_NUMBER_OF_RECORDS_PARAM, "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(Constant.HASH_1, Constant.HASH_2))))
                .andExpect(header().string(TOTAL_NUMBER_PAGES_HEADER, "1"));
    }

    @Test
    void getKnownHashpanSet_InvalidNumRecordsMin() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashpanSetMethod = KnownHashController.class.getMethod("getKnownHashpanSet", Integer.class, Integer.class, HttpServletResponse.class);
        Object[] parameterValues = {KnownHashController.MIN_VALUE - 1, 0, null};
        Set<ConstraintViolation<KnownHashControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashControllerImpl(), getKnownHashpanSetMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

    @Test
    void getKnownHashpanSet_InvalidNumRecordsMax() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashpanSetMethod = KnownHashController.class.getMethod("getKnownHashpanSet", Integer.class, Integer.class, HttpServletResponse.class);
        Object[] parameterValues = {KnownHashController.MAX_VALUE + 1, 0, null};
        Set<ConstraintViolation<KnownHashControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashControllerImpl(), getKnownHashpanSetMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

    @Test
    void getKnownHashTokenSet_onePage() {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(cardTokenRepository.findByHtokenIsNotNull(Mockito.any())).thenReturn(CardTokenRepositoryMock.getOnePageTkmCardToken());
        Set<String> knownHashpanSet = knownHashController.getKnownHashTokenSet(2, 0, httpServletResponse);
        Assertions.assertEquals(2, knownHashpanSet.size());
    }

    @Test
    void getKnownHashTokenSet_InvalidNumRecordsMin() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashpanSetMethod = KnownHashController.class.getMethod("getKnownHashTokenSet", Integer.class, Integer.class, HttpServletResponse.class);
        Object[] parameterValues = {KnownHashController.MIN_VALUE - 1, 0, null};
        Set<ConstraintViolation<KnownHashControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashControllerImpl(), getKnownHashpanSetMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

    @Test
    void getKnownHashTokenSet_InvalidNumRecordsMax() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashpanSetMethod = KnownHashController.class.getMethod("getKnownHashTokenSet", Integer.class, Integer.class, HttpServletResponse.class);
        Object[] parameterValues = {KnownHashController.MAX_VALUE + 1, 0, null};
        Set<ConstraintViolation<KnownHashControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashControllerImpl(), getKnownHashpanSetMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

}
