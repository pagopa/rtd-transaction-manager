package it.gov.pagopa.rtd.transaction_manager;

import eu.sia.meda.event.BaseEventConnector;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class PointTransactionPublisherConnector
        extends BaseEventConnector<Transaction, Boolean, Transaction, Void> {

}
