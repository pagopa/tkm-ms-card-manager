package it.gov.pagopa.tkm.ms.cardmanager.controller;

import com.fasterxml.jackson.databind.*;
import it.gov.pagopa.tkm.ms.cardmanager.config.*;
import it.gov.pagopa.tkm.ms.cardmanager.constant.*;
import it.gov.pagopa.tkm.ms.cardmanager.controller.impl.*;
import it.gov.pagopa.tkm.ms.cardmanager.model.request.*;
import it.gov.pagopa.tkm.ms.cardmanager.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.*;
import org.springframework.http.converter.xml.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TestConsentUpdateController {

    @InjectMocks
    private ConsentUpdateControllerImpl consentUpdateController;

    @Mock
    private ConsentUpdateServiceImpl consentUpdateService;

    @Spy
    private ObjectMapper mapper;

    private DefaultBeans testBeans;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(consentUpdateController)
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
    void givenValidConsentUpdateRequest_returnOk() throws Exception {
        mockMvc.perform(
                put(ApiEndpoints.BASE_PATH_CONSENT_UPDATE)
                        .content(mapper.writeValueAsString(testBeans.getConsentUpdateGlobal(ConsentEntityEnum.Allow)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        /*mockMvc.perform(
                put(ApiEndpoints.BASE_PATH_CONSENT_UPDATE)
                        .content(mapper.writeValueAsString(testBeans.getConsentUpdatePartial()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());*/
    }

    @Test
    void givenInvalidConsentUpdateRequest_expectException() throws Exception {
        ConsentResponse consentResponse = testBeans.getConsentUpdatePartial();
        consentResponse.setConsent(null);
        mockMvc.perform(
                put(ApiEndpoints.BASE_PATH_CONSENT_UPDATE)
                        .content(mapper.writeValueAsString(consentResponse))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
