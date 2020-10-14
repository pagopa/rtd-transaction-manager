package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.rtd.transaction_manager.InvoiceTransactionPublisherConnector;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the InvoiceTransactionPublisherService, defines the service used for the interaction
 * with the InvoiceTransactionPublisherConnector
 */

@Service
class InvoiceTransactionPublisherServiceImpl implements InvoiceTransactionPublisherService {

    private final InvoiceTransactionPublisherConnector invoiceTransactionPublisherConnector;
    private final SimpleEventRequestTransformer<Transaction> simpleEventRequestTransformer;
    private final SimpleEventResponseTransformer simpleEventResponseTransformer;

    @Autowired
    public InvoiceTransactionPublisherServiceImpl(InvoiceTransactionPublisherConnector invoiceTransactionPublisherConnector,
                                                  SimpleEventRequestTransformer<Transaction> simpleEventRequestTransformer,
                                                  SimpleEventResponseTransformer simpleEventResponseTransformer) {
        this.invoiceTransactionPublisherConnector = invoiceTransactionPublisherConnector;
        this.simpleEventRequestTransformer = simpleEventRequestTransformer;
        this.simpleEventResponseTransformer = simpleEventResponseTransformer;
    }

    /**
     * Calls the invoiceTransactionPublisherConnector, passing the transaction to be used as message payload
     * @param transaction
     *              Transaction instance to be used as payload for the outbound channel used bu the related connector
     */

    @Override
    public void publishInvoiceTransactionEvent(Transaction transaction) {
        invoiceTransactionPublisherConnector.doCall(
                transaction, simpleEventRequestTransformer, simpleEventResponseTransformer);
    }
}
