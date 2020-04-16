package it.gov.pagopa.rtd.transaction_manager;

import eu.sia.meda.event.BaseEventConnectorTest;
import eu.sia.meda.util.TestUtils;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import({InvoiceTransactionPublisherConnector.class})
@TestPropertySource(
        locations = "classpath:config/testInvoiceTransactionPublisher.properties",
        properties = {
                "connectors.eventConfigurations.items.InvoiceTransactionPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class InvoiceTransactionPublisherConnectorTest extends
        BaseEventConnectorTest<Transaction, Boolean, Transaction, Void, InvoiceTransactionPublisherConnector> {

    @Value("${connectors.eventConfigurations.items.InvoiceTransactionPublisherConnector.topic}")
    private String topic;

    @Autowired
    private InvoiceTransactionPublisherConnector invoiceTransactionPublisherConnector;

    @Override
    protected InvoiceTransactionPublisherConnector getEventConnector() {
        return invoiceTransactionPublisherConnector;
    }

    @Override
    protected Transaction getRequestObject() {
        return TestUtils.mockInstance(new Transaction());
    }

    @Override
    protected String getTopic() {
        return topic;
    }

}