package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.rtd.transaction_manager.InvoiceTransactionPublisherConnector;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void publishInvoiceTransactionEvent(Transaction transaction) {
        invoiceTransactionPublisherConnector.doCall(
                transaction, simpleEventRequestTransformer, simpleEventResponseTransformer);
    }
}
