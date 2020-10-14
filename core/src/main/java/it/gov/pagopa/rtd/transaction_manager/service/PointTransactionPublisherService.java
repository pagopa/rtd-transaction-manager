package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.model.Transaction;

/**
 * public interface for the PointTransactionPublisherService
 */

public interface PointTransactionPublisherService {

    /**
     * Method that has the logic for publishing a Transaction to the point-processor outbound channel,
     * calling on the appropriate connector
     * @param transaction
     *              Transaction instance to be published
     */
    void publishPointTransactionEvent(Transaction transaction);

}
