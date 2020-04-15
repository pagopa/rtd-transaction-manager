package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.rtd.transaction_manager.connector.PaymentInstrumentRestClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class PaymentInstrumentConnectorServiceTest extends BaseTest {

    @Mock
    PaymentInstrumentRestClient paymentInstrumentRestClient;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(paymentInstrumentRestClient);
    }

    @Test
    public void checkActiveTrue() {

    }

    @Test
    public void checkActiveFalse() {
    }
}