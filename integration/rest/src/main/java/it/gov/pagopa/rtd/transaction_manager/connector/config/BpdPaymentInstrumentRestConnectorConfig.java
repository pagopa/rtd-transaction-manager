package it.gov.pagopa.rtd.transaction_manager.connector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/PaymentInstrumentRestConnector.properties")
public class BpdPaymentInstrumentRestConnectorConfig {}
