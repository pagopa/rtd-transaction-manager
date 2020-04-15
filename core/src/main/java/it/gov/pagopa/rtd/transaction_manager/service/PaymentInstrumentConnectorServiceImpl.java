package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.connector.PaymentInstrumentRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class PaymentInstrumentConnectorServiceImpl
        implements PaymentInstrumentConnectorService {

    private final PaymentInstrumentRestClient paymentInstrumentRestClient;

    @Autowired
    public PaymentInstrumentConnectorServiceImpl(PaymentInstrumentRestClient paymentInstrumentRestClient) {
        this.paymentInstrumentRestClient = paymentInstrumentRestClient;
    }

    @Override
    public Boolean checkActive(String hpan, OffsetDateTime accountingDate) {
        return paymentInstrumentRestClient.checkActive(hpan, accountingDate);
    }

}
