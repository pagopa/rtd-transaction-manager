package it.gov.pagopa.rtd.transaction_manager.connector.config;

import it.gov.pagopa.bpd.common.connector.config.RestConnectorConfig;
import it.gov.pagopa.rtd.transaction_manager.connector.FaMerchantRestClient;
import it.gov.pagopa.rtd.transaction_manager.connector.FaPaymentInstrumentRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestConnectorConfig.class)
@EnableFeignClients(clients = FaMerchantRestClient.class)
@PropertySource("classpath:config/fa/merchant-rest-client.properties")
public class FaMerchantRestConnectorConfig {
}
