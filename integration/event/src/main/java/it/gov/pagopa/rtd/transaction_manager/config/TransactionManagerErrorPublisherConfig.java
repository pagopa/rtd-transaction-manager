package it.gov.pagopa.rtd.transaction_manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author ALessio Cialini
 * Configuration class for the TransactionManagerErrorPublisherConnector
 */

@Configuration
@PropertySource("classpath:config/transactionManagerErrorPublisher.properties")
public class TransactionManagerErrorPublisherConfig { }
