package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.connector.model.MerchantResource;

/**
 * public interface for the FaMerchantShopConnectorService
 */


public interface FaMerchantConnectorService {

    public MerchantResource findMerchant(String merchantId);

}
