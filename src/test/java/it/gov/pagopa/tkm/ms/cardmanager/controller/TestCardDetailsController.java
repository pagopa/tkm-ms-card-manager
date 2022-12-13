package it.gov.pagopa.tkm.ms.cardmanager.controller;

import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.config.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.controller.impl.*;
import it.gov.pagopa.tkm.ms.cardmanager.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.*;
import org.springframework.http.converter.xml.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.*;

import static it.gov.pagopa.tkm.ms.cardmanager.constant.ApiParams.HPAN_HEADER;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TestCardDetailsController {

    @InjectMocks
    private CardDetailsControllerImpl cardDetailsController;

    @Mock
    private CardRepository cardRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private DefaultBeans testBeans;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cardDetailsController)
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
    void givenValidRequest_returnValidResponse() throws Exception {
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(testBeans.TKM_CARD_PAN_PAR_1);
        mockMvc.perform(
                get(ApiEndpoints.BASE_PATH_CARDS)
                        .header(HPAN_HEADER, testBeans.HPAN_1))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(testBeans.CARD_DETAILS)));
    }

    @Test
    void givenNonexistentHpan_return404() throws Exception {
        when(cardRepository.findByHpan(testBeans.HPAN_1)).thenReturn(null);
        mockMvc.perform(
                        get(ApiEndpoints.BASE_PATH_CARDS)
                                .header(HPAN_HEADER, testBeans.HPAN_1))
                .andExpect(status().isNotFound());
    }

}
