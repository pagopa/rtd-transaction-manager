package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.connector.FaPaymentInstrumentRestClient;
import it.gov.pagopa.rtd.transaction_manager.connector.model.PaymentInstrumentResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the PaymentInstrumentConnectorService, it's responsible for calling the connector related
 * to the REST endpoint for the checking active payment instruments in the related microservice
 */

@Service
@Slf4j
class FaPaymentInstrumentConnectorServiceImpl
        implements FaPaymentInstrumentConnectorService {

    private final FaPaymentInstrumentRestClient faPaymentInstrumentRestClient;

    @Autowired
    public FaPaymentInstrumentConnectorServiceImpl(FaPaymentInstrumentRestClient faPaymentInstrumentRestClient) {
        this.faPaymentInstrumentRestClient = faPaymentInstrumentRestClient;
    }

    /**
     * Calls the RestClient for the checkStatus endpoint, using the inbound Transaction data
     *
     * @param hpan Hpan from the inbound transaction, used to identify the payment instrument to check
     * @return PaymentInstrumentResource in order to defining if the instrument is active
     */

    @Override
    public PaymentInstrumentResource find(String hpan) {
        return faPaymentInstrumentRestClient.find(hpan);
    }

}
