package it.gov.pagopa.rtd.transaction_manager.service;

import java.time.OffsetDateTime;

public interface PaymentInstrumentConnectorService {

    public Boolean checkActive(String hpan, OffsetDateTime accountingDate) throws Exception;

}
