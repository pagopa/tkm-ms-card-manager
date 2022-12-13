package it.gov.pagopa.tkm.ms.cardmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.tkm.ms.cardmanager.config.ErrorHandler;
import it.gov.pagopa.tkm.ms.cardmanager.constant.ApiEndpoints;
import it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams;
import it.gov.pagopa.tkm.ms.cardmanager.constant.DefaultBeans;
import it.gov.pagopa.tkm.ms.cardmanager.controller.impl.ParlessCardsControllerImpl;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.ParlessCardsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TestParlessCardsController {

    @InjectMocks
    private ParlessCardsControllerImpl parlessCardsController;

    @Mock
    private ParlessCardsServiceImpl parlessCardsService;

    private final ObjectMapper mapper = new ObjectMapper();

    private DefaultBeans testBeans;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(parlessCardsController)
                .setMessageConverters(
                        new ByteArrayHttpMessageConverter(),
                        new StringHttpMessageConverter(),
                        new ResourceHttpMessageConverter(),
                        new FormHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(),
                        new Jaxb2RootElementHttpMessageConverter())
                .setControllerAdvice(new ErrorHandler())
                .build();
        testBeans = new DefaultBeans();
    }

    @Test
    void givenValidParlessCardsRequest_returnValidParlessCardsResponse() throws Exception {
        when(parlessCardsService.getParlessCards(2)).thenReturn(testBeans.PARLESS_CARD_LIST);
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_PARLESS_CARDS)
                        .queryParam(ApiParams.MAX_NUMBER_OF_CARDS_PARAM, "2"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(testBeans.PARLESS_CARD_LIST)));
    }

    @Test
    void givenInvalidParlessCardsRequest_expectException() throws Exception {
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_PARLESS_CARDS))
                .andExpect(status().isBadRequest());
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_PARLESS_CARDS)
                        .queryParam(ApiParams.MAX_NUMBER_OF_CARDS_PARAM, "a"))
                .andExpect(status().isBadRequest());
    }

}
