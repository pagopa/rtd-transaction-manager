package it.gov.pagopa.rtd.transaction_manager.connector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for the PointTransactionPublisherConnector
 */


@Configuration
@PropertySource("classpath:config/PaymentInstrumentRestConnector.properties")
public class OldBpdPaymentInstrumentRestConnectorConfig {
}
