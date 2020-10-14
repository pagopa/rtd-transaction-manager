package it.gov.pagopa.rtd.transaction_manager.connector;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.gov.pagopa.bpd.common.connector.BaseFeignRestClientTest;
import it.gov.pagopa.rtd.transaction_manager.connector.config.FaPaymentInstrumentRestConnectorConfig;
import it.gov.pagopa.rtd.transaction_manager.connector.model.PaymentInstrumentResource;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static junit.framework.TestCase.assertNotNull;

@TestPropertySource(
        locations = "classpath:config/fa/pi-rest-client.properties",
        properties = "spring.application.name=rtd-ms-transaction-manager-integration-rest")
@ContextConfiguration(initializers = FaPaymentInstrumentRestClientTest.RandomPortInitializer.class,
        classes = FaPaymentInstrumentRestConnectorConfig.class)
public class FaPaymentInstrumentRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    static {
        String port = System.getenv("WIREMOCKPORT");
        wireMockRule = new WireMockClassRule(wireMockConfig()
                .port(port != null ? Integer.parseInt(port) : 0)
                .bindAddress("localhost")
                .usingFilesUnderClasspath("stubs/payment-instrument")
                .extensions(new ResponseTemplateTransformer(false))
        );
    }

    @Autowired
    private FaPaymentInstrumentRestClient restClient;


    @Test
    public void find() {
        final String hpan = "hpan";

        final PaymentInstrumentResource actualResponse = restClient.find(hpan);

        assertNotNull(actualResponse);
    }


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils
                    .addInlinedPropertiesToEnvironment(applicationContext,
                            String.format("rest-client.fa-payment-instrument.base-url=http://%s:%d/fa/payment-instruments",
                                    wireMockRule.getOptions().bindAddress(),
                                    wireMockRule.port())
                    );
        }
    }
}
