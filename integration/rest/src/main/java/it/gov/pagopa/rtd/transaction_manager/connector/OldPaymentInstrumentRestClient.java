package it.gov.pagopa.rtd.transaction_manager.connector;

import java.time.OffsetDateTime;

/**
 * public interface of the OldPaymentInstrumentRestClient, provides a callable method to the related core service
 */

public interface OldPaymentInstrumentRestClient {

    Boolean checkActive(String hpan, OffsetDateTime accountingDate);

}
