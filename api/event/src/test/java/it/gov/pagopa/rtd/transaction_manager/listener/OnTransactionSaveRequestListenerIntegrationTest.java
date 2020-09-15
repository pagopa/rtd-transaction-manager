package it.gov.pagopa.rtd.transaction_manager.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import eu.sia.meda.event.service.ErrorPublisherService;
import eu.sia.meda.eventlistener.BaseEventListenerIntegrationTest;
import it.gov.pagopa.rtd.transaction_manager.factory.SaveTransactionCommandModelFactory;
import it.gov.pagopa.rtd.transaction_manager.listener.config.TestConfig;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.TransactionManagerErrorPublisherService;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.ClassRule;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.configuration.ObjectPostProcessorConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Integration Testing class for the whole microservice, it executes the entire flow starting from the
 * inbound event listener, to the production of a message in the outbound channel
 */

@EnableConfigurationProperties
@ContextConfiguration(initializers = OnTransactionSaveRequestListenerIntegrationTest.RandomPortInitializer.class,
        classes = {
                TestConfig.class,
                RestTemplateAutoConfiguration.class,
                JacksonAutoConfiguration.class,
                ObjectPostProcessorConfiguration.class,
                AuthenticationConfiguration.class,
                KafkaAutoConfiguration.class,
                FeignAutoConfiguration.class
        })
@TestPropertySource(
        locations = {
                "classpath:config/testTransactionRequestListener.properties",
                "classpath:config/testInvoiceTransactionPublisher.properties",
                "classpath:config/testPointTransactionPublisher.properties",
                "classpath:config/testTransactionManagerErrorPublisher.properties"
        },
        properties = {
                "listeners.eventConfigurations.items.OnTransactionSaveRequestListener.bootstrapServers=${spring.embedded.kafka.brokers}",
                "connectors.eventConfigurations.items.InvoiceTransactionPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}",
                "connectors.eventConfigurations.items.PointTransactionPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}",
                "connectors.eventConfigurations.items.TransactionManagerErrorPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class OnTransactionSaveRequestListenerIntegrationTest extends BaseEventListenerIntegrationTest {

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
            .port(8080)
            .usingFilesUnderClasspath("stubs/payment-instrument")
    );

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

            TestPropertySourceUtils
                    .addInlinedPropertiesToEnvironment(applicationContext,
                            String.format("rest-client.fa-payment-instrument.base-url=http://%s:%d/fa/payment-instruments",
                                    wireMockRule.getOptions().bindAddress(),
                                    wireMockRule.port())

                    );

        }


    }

    @Value("${listeners.eventConfigurations.items.OnTransactionSaveRequestListener.topic}")
    private String topicSubscription;
    @Value("${connectors.eventConfigurations.items.PointTransactionPublisherConnector.topic}")
    private String topicPublished;

    @Override
    protected ErrorPublisherService getErrorPublisherService() {
        return null;
    }

    @SpyBean
    private PaymentInstrumentConnectorService paymentInstrumentConnectorServiceSpy;

    @SpyBean
    private PointTransactionPublisherService pointTransactionPublisherServiceSpy;

    @SpyBean
    private InvoiceTransactionPublisherService invoiceTransactionPublisherServiceSpy;

    @SpyBean
    SaveTransactionCommandModelFactory saveTransactionCommandModelFactorySpy;

    @SpyBean
    TransactionManagerErrorPublisherService transactionManagerErrorPublisherService;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    protected Object getRequestObject() {
        return Transaction.builder()
                .idTrxAcquirer("1")
                .acquirerCode("001")
                .trxDate(OffsetDateTime.parse("2020-04-10T14:59:59.245Z"))
                .amount(BigDecimal.valueOf(1313.13))
                .operationType("00")
                .hpan("test")
                .merchantId("0")
                .circuitType("00")
                .mcc("813")
                .idTrxIssuer("0")
                .amountCurrency("833")
                .correlationId("1")
                .acquirerId("0")
                .terminalId("2")
                .bin("000001")
                .build();
    }

    @Override
    protected String getTopicSubscription() {
        return topicSubscription;
    }

    @Override
    protected String getTopicPublished() {
        return topicPublished;
    }

    @Override
    protected void verifyPublishedMessages(List<ConsumerRecord<String, String>> records) {

        try {

            Transaction sentTransaction = (Transaction) getRequestObject();
            sentTransaction.setTrxDate(OffsetDateTime.parse("2020-04-10T16:59:59.245+02:00"));
            Assert.assertEquals(1, records.size());
            Transaction publishedTransaction = objectMapper.readValue(records.get(0).value(), Transaction.class);
            Assert.assertEquals(sentTransaction, publishedTransaction);
            BDDMockito.verify(paymentInstrumentConnectorServiceSpy, Mockito.atLeastOnce())
                    .checkActive(Mockito.eq(sentTransaction.getHpan()),
                            Mockito.eq(sentTransaction.getTrxDate()));
            BDDMockito.verify(pointTransactionPublisherServiceSpy).publishPointTransactionEvent(Mockito.any());
            BDDMockito.verify(invoiceTransactionPublisherServiceSpy).publishInvoiceTransactionEvent(Mockito.any());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

}