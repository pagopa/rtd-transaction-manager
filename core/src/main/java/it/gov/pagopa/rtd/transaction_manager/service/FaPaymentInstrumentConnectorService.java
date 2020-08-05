package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.connector.model.PaymentInstrumentResource;

/**
 * public interface for the PaymentInstrumentConnectorService
 */

public interface FaPaymentInstrumentConnectorService {

    /**
     * Method that has the logic for recovering the status for a given instrument on the given date,
     * calling on the appropriate REST connector
     *
     * @param hpan hpan of the instrument to check
     */

    public PaymentInstrumentResource find(String hpan);


}
