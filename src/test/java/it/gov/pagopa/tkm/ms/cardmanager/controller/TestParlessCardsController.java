package it.gov.pagopa.tkm.ms.cardmanager.controller;

import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.config.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.controller.impl.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.http.converter.json.*;
import org.springframework.http.converter.xml.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TestParlessCardsController {

    @InjectMocks
    private ParlessCardsControllerImpl parlessCardsController;

    @Mock
    private ParlessCardsServiceImpl parlessCardsService;

    private final ObjectMapper mapper = new ObjectMapper();

    private DefaultBeans testBeans;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(parlessCardsController)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(),
                        new MappingJackson2XmlHttpMessageConverter())
                .setControllerAdvice(new ErrorHandler())
                .build();
        testBeans = new DefaultBeans();
    }

    @Test
    public void givenValidParlessCardsRequest_returnValidParlessCardsResponse() throws Exception {
        when(parlessCardsService.getParlessCards(2)).thenReturn(testBeans.PARLESS_CARD_LIST);
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_PARLESS_CARDS)
                        .queryParam(ApiParams.MAX_NUMBER_OF_CARDS, "2"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(testBeans.PARLESS_CARD_LIST)));
    }

    @Test
    public void givenInvalidParlessCardsRequest_expectException() throws Exception {
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_PARLESS_CARDS))
                .andExpect(status().isBadRequest());
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_PARLESS_CARDS)
                    .queryParam(ApiParams.MAX_NUMBER_OF_CARDS, "a"))
                .andExpect(status().isBadRequest());
    }

}
