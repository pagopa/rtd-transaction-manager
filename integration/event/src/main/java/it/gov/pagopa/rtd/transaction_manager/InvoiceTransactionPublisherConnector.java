package it.gov.pagopa.rtd.transaction_manager;

import eu.sia.meda.event.BaseEventConnector;
import eu.sia.meda.event.transformer.IEventRequestTransformer;
import eu.sia.meda.event.transformer.IEventResponseTransformer;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class InvoiceTransactionPublisherConnector
        extends BaseEventConnector<Transaction, Boolean, Transaction, Void> {

    public Boolean doCall(
            Transaction transaction, IEventRequestTransformer<Transaction,
            Transaction> requestTransformer,
            IEventResponseTransformer<Void, Boolean> responseTransformer,
            Object... args) {
        return this.call(transaction, requestTransformer, responseTransformer, args);
    }
    
}
