package it.gov.pagopa.rtd.transaction_manager;

import eu.sia.meda.event.BaseEventConnector;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;

//@Service
public class InvoiceTransactionPublisherConnector
        extends BaseEventConnector<Transaction, Boolean, Transaction, Void> {

}
