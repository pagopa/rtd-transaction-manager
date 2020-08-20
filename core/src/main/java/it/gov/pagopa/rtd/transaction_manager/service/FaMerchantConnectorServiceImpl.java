package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.connector.FaMerchantRestClient;
import it.gov.pagopa.rtd.transaction_manager.connector.FaPaymentInstrumentRestClient;
import it.gov.pagopa.rtd.transaction_manager.connector.model.MerchantResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FaMerchantConnectorServiceImpl implements FaMerchantConnectorService {

    private final FaMerchantRestClient faMerchantRestClient;

    @Autowired
    public FaMerchantConnectorServiceImpl(FaMerchantRestClient faMerchantRestClient) {
        this.faMerchantRestClient = faMerchantRestClient;
    }

    @Override
    public MerchantResource findMerchant(String merchantId) {
        return faMerchantRestClient.findMerchantId(merchantId);
    }
}
