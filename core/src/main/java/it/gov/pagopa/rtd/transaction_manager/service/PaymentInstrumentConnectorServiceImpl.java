package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.connector.PaymentInstrumentRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * @author Alessio Cialini
 * Implementation of the PaymentInstrumentConnectorService, it's responsible for calling the connector related
 * to the REST endpoint for the checking active payment instruments in the related microservice
 */

@Service
@Slf4j
class PaymentInstrumentConnectorServiceImpl
        implements PaymentInstrumentConnectorService {

    private final PaymentInstrumentRestClient paymentInstrumentRestClient;

    @Autowired
    public PaymentInstrumentConnectorServiceImpl(PaymentInstrumentRestClient paymentInstrumentRestClient) {
        this.paymentInstrumentRestClient = paymentInstrumentRestClient;
    }

    /**
     * Calls the RestClient for the checkActive endpoint, using the inbound Transaction data
     * @param hpan
     *          Hpan from the inbound transaction, used to identify the payment instrument to check
     * @param accountingDate
     *          Date to be used for checking if the payment instrument is active
     * @return Boolean defining if the instrument is active
     */
    @Override
    public Boolean checkActive(String hpan, OffsetDateTime accountingDate) {
        return paymentInstrumentRestClient.checkActive(hpan, accountingDate);
    }

}
