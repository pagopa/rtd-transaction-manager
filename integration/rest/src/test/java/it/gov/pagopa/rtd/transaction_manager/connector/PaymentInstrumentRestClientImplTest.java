package it.gov.pagopa.rtd.transaction_manager.connector;

import eu.sia.meda.connector.meda.ArchMedaInternalConnectorConfigurationService;
import eu.sia.meda.connector.rest.BaseRestConnectorTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;

/**
 * @author Alessio Cialini
 * test class for the REST client
 */

@Import({PaymentInstrumentRestClientImpl.class, PaymentInstrumentRestConnector.class, ArchMedaInternalConnectorConfigurationService.class})
@TestPropertySource(
        locations = {
                "classpath:config/PaymentInstrumentRestConnector.properties"
        },
        properties = {
                "connectors.medaInternalConfigurations.items.PaymentInstrumentRestConnector.mocked=true",
                "connectors.medaInternalConfigurations.items.PaymentInstrumentRestConnector.path=payment-instrument/test/history"
        })
public class PaymentInstrumentRestClientImplTest extends BaseRestConnectorTest {

    @Autowired
    PaymentInstrumentRestClient paymentInstrumentRestClient;

    @Test
    public void checkActive() {
        try {
            Boolean isActive = paymentInstrumentRestClient.checkActive(
                    "test", OffsetDateTime.parse("2020-04-10T14:59:59.245Z"));
            Assert.assertTrue(isActive);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}