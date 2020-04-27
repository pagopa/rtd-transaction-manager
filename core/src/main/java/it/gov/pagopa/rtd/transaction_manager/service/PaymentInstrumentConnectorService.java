package it.gov.pagopa.rtd.transaction_manager.service;

import java.time.OffsetDateTime;

/**
 * @author Alessio Cialini
 * public interface for the PaymentInstrumentConnectorService
 */

public interface PaymentInstrumentConnectorService {

    /**
     * Method that has the logic for recovering the status for a given instrument on the given date,
     * calling on the appropriate REST connector
     * @param hpan
     *          hpan of the instrument to check
     */

    public Boolean checkActive(String hpan, OffsetDateTime accountingDate) throws Exception;

}
