package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.model.Transaction;

/**
 * @author Alessio Cialini
 * public interface for the InvoiceTransactionPublisherService
 */

public interface InvoiceTransactionPublisherService {

    /**
     * Method that has the logic for publishing a Transaction to the invoice outbound channel,
     * calling on the appropriate connector
     * @param transaction
     *              Transaction instance to be published
     */

    void publishInvoiceTransactionEvent(Transaction transaction);

}
