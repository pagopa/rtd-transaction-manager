package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.rtd.transaction_manager.connector.PaymentInstrumentRestClient;
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

public class PaymentInstrumentConnectorServiceTest extends BaseTest {

    @Mock
    PaymentInstrumentRestClient paymentInstrumentRestClient;

    @Autowired
    PaymentInstrumentConnectorService paymentInstrumentConnectorService;

    private final OffsetDateTime accountingDate = OffsetDateTime.now();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        Mockito.reset(paymentInstrumentRestClient);
        paymentInstrumentConnectorService = new PaymentInstrumentConnectorServiceImpl(paymentInstrumentRestClient);
    }

    @Test
    public void checkActiveTrue() {
        BDDMockito.doReturn(true).when(paymentInstrumentRestClient)
                .checkActive(Mockito.eq("activepan"), Mockito.eq(accountingDate));
        try {
            Boolean isActive = paymentInstrumentConnectorService.checkActive("activepan", accountingDate);
            Assert.assertTrue(isActive);
            BDDMockito.verify(paymentInstrumentRestClient, Mockito.atLeastOnce()).checkActive(
                    Mockito.eq("activepan"), Mockito.eq(accountingDate));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void checkActiveFalse() {
        BDDMockito.doReturn(false).when(paymentInstrumentRestClient)
                .checkActive(Mockito.eq("notactivepan"), Mockito.eq(accountingDate));
        try {
            Boolean isActive = paymentInstrumentConnectorService.checkActive("notactivepan", accountingDate);
            Assert.assertFalse(isActive);
            BDDMockito.verify(paymentInstrumentRestClient, Mockito.atLeastOnce()).checkActive(
                    Mockito.eq("notactivepan"), Mockito.eq(accountingDate));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @SneakyThrows
    @Test
    public void connector_KO() {

        BDDMockito.doAnswer(invocationOnMock -> {
            throw new Exception();
        }).when(paymentInstrumentRestClient)
                .checkActive(Mockito.any(), Mockito.any());

        expectedException.expect(Exception.class);
        paymentInstrumentConnectorService.checkActive("notactivepan", accountingDate);
        BDDMockito.verify(paymentInstrumentRestClient, Mockito.atLeastOnce()).checkActive(
                Mockito.eq("notactivepan"), Mockito.eq(accountingDate));
    }

}