package it.gov.pagopa.rtd.transaction_manager.connector;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.gov.pagopa.bpd.common.connector.BaseFeignRestClientTest;
import it.gov.pagopa.rtd.transaction_manager.connector.config.FaMerchantRestConnectorConfig;
import it.gov.pagopa.rtd.transaction_manager.connector.config.FaPaymentInstrumentRestConnectorConfig;
import it.gov.pagopa.rtd.transaction_manager.connector.model.MerchantResource;
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
        locations = "classpath:config/fa/merchant-rest-client.properties",
        properties = "spring.application.name=rtd-ms-transaction-manager-integration-rest")
@ContextConfiguration(initializers = FaMerchantRestClientTest.RandomPortInitializer.class,
        classes = FaMerchantRestConnectorConfig.class)
public class FaMerchantRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
            .dynamicPort()
            .usingFilesUnderClasspath("stubs/merchant")
            .extensions(new ResponseTemplateTransformer(false))
    );

    @Autowired
    private FaMerchantRestClient restClient;

    @Test
    public void findMerchantId() {
        final String merchantId = "merchantId";

        final MerchantResource actualResponse = restClient.findMerchantId(merchantId);

        assertNotNull(actualResponse);
    }


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils
                    .addInlinedPropertiesToEnvironment(applicationContext,
                            String.format("rest-client.fa-merchant.base-url=http://%s:%d/fa/merchant",
                                    wireMockRule.getOptions().bindAddress(),
                                    wireMockRule.port())
                    );
        }
    }

}
