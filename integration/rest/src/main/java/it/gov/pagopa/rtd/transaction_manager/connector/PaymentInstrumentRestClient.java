package it.gov.pagopa.rtd.transaction_manager.connector;

import java.time.OffsetDateTime;

public interface PaymentInstrumentRestClient {

    Boolean checkActive(String hpan, OffsetDateTime accountingDate);

}
