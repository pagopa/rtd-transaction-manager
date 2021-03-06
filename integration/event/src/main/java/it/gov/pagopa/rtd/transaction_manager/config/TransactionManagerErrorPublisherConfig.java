package it.gov.pagopa.rtd.transaction_manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for the TransactionManagerErrorPublisherConnector
 */

@Configuration
@PropertySource("classpath:config/transactionManagerErrorPublisher.properties")
public class TransactionManagerErrorPublisherConfig { }
