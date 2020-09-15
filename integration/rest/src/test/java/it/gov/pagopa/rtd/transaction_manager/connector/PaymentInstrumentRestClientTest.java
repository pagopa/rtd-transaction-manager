package it.gov.pagopa.rtd.transaction_manager.connector;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.gov.pagopa.bpd.common.connector.BaseFeignRestClientTest;
import it.gov.pagopa.rtd.transaction_manager.connector.config.BpdPaymentInstrumentRestConnectorConfig;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.time.OffsetDateTime;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TestPropertySource(
        locations = "classpath:config/bpd/rest-client.properties",
        properties = "spring.application.name=rtd-ms-transaction-manager-integration-rest")
@ContextConfiguration(initializers = PaymentInstrumentRestClientTest.RandomPortInitializer.class,
        classes = BpdPaymentInstrumentRestConnectorConfig.class)
public class PaymentInstrumentRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    static {
        String port = System.getenv("WIREMOCK_PORT");
        wireMockRule = new WireMockClassRule(wireMockConfig()
                .port(port != null ? Integer.parseInt(port) : 0)
                .bindAddress("localhost")
                .usingFilesUnderClasspath("stubs/payment-instrument")
                .extensions(new ResponseTemplateTransformer(false))
        );
    }

    @Test
    public void checkActive() {
        final String hashPan = "hashPan-active";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2020-04-10T14:59:59.245Z");

        final boolean actualResponse = restClient.checkActive(hashPan, offsetDateTime);

        assertTrue(actualResponse);
    }

    @Autowired
    private PaymentInstrumentRestClient restClient;

    @Test
    public void checkNotActive() {
        final String hashPan = "hashPan-inactive";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2020-04-10T14:59:59.245Z");

        final boolean actualResponse = restClient.checkActive(hashPan, offsetDateTime);

        assertFalse(actualResponse);
    }

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils
                    .addInlinedPropertiesToEnvironment(applicationContext,
                            String.format("rest-client.payment-instrument.base-url=http://%s:%d/bpd/payment-instruments",
                                    wireMockRule.getOptions().bindAddress(),
                                    wireMockRule.port())
                    );
        }
    }
}