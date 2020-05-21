package it.gov.pagopa.rtd.transaction_manager.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.bpd.common.connector.BaseFeignRestClientTest;
import it.gov.pagopa.rtd.transaction_manager.connector.config.BpdPaymentInstrumentRestConnectorConfig;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

@TestPropertySource(
        locations = "classpath:config/rest-client.properties",
        properties = "spring.application.name=rtd-ms-transaction-manager-integration-rest")
@Import({BpdPaymentInstrumentRestConnectorConfig.class})
public class PaymentInstrumentRestClientTest extends BaseFeignRestClientTest {

    static {
        SERIVICE_PORT_ENV_VAR_NAME = "BPD_PAYMENT_INSTRUMENT_PORT";
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentInstrumentRestClient restClient;

    @Test
    public void checkActive() throws IOException {

        final String hashPan = "hashPan";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2020-04-10T14:59:59.245Z");
        String encOffsetDateTime = URLEncoder.encode("2020-04-10T14:59:59.245Z", StandardCharsets.UTF_8.toString());

        final boolean expectedResponse = true;
        stubFor(get(urlEqualTo("/bpd/payment-instruments/"
                + hashPan + "/history/active?accountingDate=" + encOffsetDateTime))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .withBody(objectMapper.writeValueAsString(expectedResponse))));

        final boolean actualResponse = restClient.checkActive(hashPan, offsetDateTime);

        assertEquals(actualResponse, expectedResponse);
    }

    @Test
    public void checkNotActive() throws IOException {

        final String hashPan = "hashPan";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2020-04-10T14:59:59.245Z");
        String encOffsetDateTime = URLEncoder.encode("2020-04-10T14:59:59.245Z", StandardCharsets.UTF_8.toString());

        final boolean expectedResponse = false;
        stubFor(get(urlEqualTo("/bpd/payment-instruments/"
                + hashPan + "/history/active?accountingDate=" + encOffsetDateTime))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .withBody(objectMapper.writeValueAsString(expectedResponse))));

        final boolean actualResponse = restClient.checkActive(hashPan, offsetDateTime);

        assertEquals(actualResponse, expectedResponse);
    }
}