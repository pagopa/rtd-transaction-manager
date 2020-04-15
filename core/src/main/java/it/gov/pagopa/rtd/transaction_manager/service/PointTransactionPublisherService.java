package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.model.Transaction;

public interface PointTransactionPublisherService {

    void publishPointTransactionEvent(Transaction transaction);

}
