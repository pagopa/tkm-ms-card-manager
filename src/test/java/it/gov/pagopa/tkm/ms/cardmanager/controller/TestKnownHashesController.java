package it.gov.pagopa.tkm.ms.cardmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.config.ErrorHandler;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.controller.impl.KnownHashesControllerImpl;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCard;
import it.gov.pagopa.tkm.ms.cardmanager.model.entity.TkmCardToken;
import it.gov.pagopa.tkm.ms.cardmanager.model.response.KnownHashesResponse;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardRepository;
import it.gov.pagopa.tkm.ms.cardmanager.repository.CardTokenRepository;
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
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TestKnownHashesController {

    @InjectMocks
    private KnownHashesControllerImpl knownHashController;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardTokenRepository cardTokenRepository;

    @Mock
    private QueryServiceMultiThreading queryServiceMultiThreading;

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
    void getKnownHashes() throws Exception {
        ReflectionTestUtils.setField(knownHashController, "threadKnownHashes", 1);
        Mockito.when(queryServiceMultiThreading.getTkmCardSubSetAsync(Mockito.anyLong(),Mockito.anyLong())).thenReturn(new AsyncResult(CardRepositoryMock.getTkmCardsSubSetList()));
        Mockito.when(queryServiceMultiThreading.getTkmCardTokenSubSetAsync(Mockito.anyLong(),Mockito.anyLong())).thenReturn(new AsyncResult(CardTokenRepositoryMock.getTkmCardTokensList()));
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_KNOWN_HASHES)
                        .queryParam(ApiParams.MAX_NUMBER_OF_RECORDS_PARAM, "4")
                        .queryParam(ApiParams.HPAN_OFFSET_PARAM, "2")
                        .queryParam(ApiParams.HTOKEN_OFFSET_PARAM, "2")
        )
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(
                        new KnownHashesResponse(
                                new HashSet<>(Arrays.asList(Constant.HASH_1, Constant.HASH_2)),
                                new HashSet<>(Arrays.asList(Constant.HASH_1, Constant.HASH_2)),
                                2L,
                                2L
                        ))));
    }

    @Test
    void getKnownHashesWithFirstIdNon1() throws Exception {
        when(cardRepository.findTopByOrderByIdAsc()).thenReturn(TkmCard.builder().id(1000L).build());
        when(cardTokenRepository.findTopByOrderByIdAsc()).thenReturn(TkmCardToken.builder().id(1000L).build());
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_KNOWN_HASHES)
                        .queryParam(ApiParams.MAX_NUMBER_OF_RECORDS_PARAM, "4")
                        .queryParam(ApiParams.HPAN_OFFSET_PARAM, "2")
                        .queryParam(ApiParams.HTOKEN_OFFSET_PARAM, "2")
        )
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(
                        new KnownHashesResponse(
                                new HashSet<>(),
                                new HashSet<>(),
                                999L,
                                999L
                        ))));
    }

    @Test
    void getKnownHashes_InvalidNumRecordsMin() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashesMethod = KnownHashesController.class.getMethod("getKnownHashes", Long.class, Long.class, Long.class);
        Object[] parameterValues = {KnownHashesController.MIN_VALUE - 1L, 0L, null};
        Set<ConstraintViolation<KnownHashesControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashesControllerImpl(), getKnownHashesMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

    @Test
    void getKnownHashes_InvalidNumRecordsMax() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Method getKnownHashesMethod = KnownHashesController.class.getMethod("getKnownHashes", Long.class, Long.class, Long.class);
        Object[] parameterValues = {KnownHashesController.MAX_VALUE + 1L, 0L, null};
        Set<ConstraintViolation<KnownHashesControllerImpl>> constraintViolations = validator.validateParameters(new KnownHashesControllerImpl(), getKnownHashesMethod, parameterValues);
        assertEquals(1, constraintViolations.size());
    }

}
