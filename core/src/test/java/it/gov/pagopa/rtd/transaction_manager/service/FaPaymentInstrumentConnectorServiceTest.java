package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.rtd.transaction_manager.connector.FaPaymentInstrumentRestClient;
import it.gov.pagopa.rtd.transaction_manager.connector.model.PaymentInstrumentResource;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

/**
 * Test class for the PaymentInstrumentConnectorService method
 */

public class FaPaymentInstrumentConnectorServiceTest extends BaseTest {

    private final OffsetDateTime accountingDate = OffsetDateTime.now();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    FaPaymentInstrumentRestClient faPaymentInstrumentRestClient;
    @Autowired
    FaPaymentInstrumentConnectorService faPaymentInstrumentConnectorService;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(faPaymentInstrumentRestClient);
        faPaymentInstrumentConnectorService = new FaPaymentInstrumentConnectorServiceImpl(faPaymentInstrumentRestClient);
    }


    @Test
    public void checkStatus() {
        PaymentInstrumentResource paymentInstrument = new PaymentInstrumentResource();
        paymentInstrument.setStatus(PaymentInstrumentResource.Status.ACTIVE);

        BDDMockito.doReturn(paymentInstrument).when(faPaymentInstrumentRestClient)
                .find(Mockito.eq("activepan"));
        try {
            PaymentInstrumentResource resource = faPaymentInstrumentConnectorService.find("activepan");
            Assert.assertNotNull(resource);
            BDDMockito.verify(faPaymentInstrumentRestClient, Mockito.atLeastOnce()).find(
                    Mockito.eq("activepan"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void checkStatus_ko() {
        PaymentInstrumentResource paymentInstrument = new PaymentInstrumentResource();
        paymentInstrument.setStatus(PaymentInstrumentResource.Status.INACTIVE);

        BDDMockito.doReturn(null).when(faPaymentInstrumentRestClient)
                .find(Mockito.eq("notactivepan"));
        try {
            PaymentInstrumentResource resource = faPaymentInstrumentConnectorService.find("notactivepan");
            Assert.assertNull(resource);
            BDDMockito.verify(faPaymentInstrumentRestClient, Mockito.atLeastOnce()).find(
                    Mockito.eq("notactivepan"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @SneakyThrows
    @Test
    public void fa_connector_KO() {

        BDDMockito.doAnswer(invocationOnMock -> {
            throw new Exception();
        }).when(faPaymentInstrumentRestClient)
                .find(Mockito.any());

        expectedException.expect(Exception.class);
        faPaymentInstrumentConnectorService.find("notactivepan");
        BDDMockito.verify(faPaymentInstrumentRestClient, Mockito.atLeastOnce()).find(
                Mockito.eq("notactivepan"));
    }
}