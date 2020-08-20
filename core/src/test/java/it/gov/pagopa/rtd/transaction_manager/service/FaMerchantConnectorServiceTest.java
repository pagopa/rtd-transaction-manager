package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.rtd.transaction_manager.connector.FaMerchantRestClient;
import it.gov.pagopa.rtd.transaction_manager.connector.FaPaymentInstrumentRestClient;
import it.gov.pagopa.rtd.transaction_manager.connector.model.MerchantResource;
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
 * Test class for the FaMerchantConnectorService method
 */

public class FaMerchantConnectorServiceTest extends BaseTest {

    private final OffsetDateTime accountingDate = OffsetDateTime.now();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    FaMerchantRestClient faMerchantRestClient;
    @Autowired
    FaMerchantConnectorService faMerchantConnectorService;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(faMerchantRestClient);
        faMerchantConnectorService = new FaMerchantConnectorServiceImpl(faMerchantRestClient);
    }


    @Test
    public void findMerchant_OK() {
        MerchantResource merchantResource = new MerchantResource();
        merchantResource.setTimestampTC(OffsetDateTime.parse("2020-04-09T15:22:45.304Z"));

        BDDMockito.doReturn(merchantResource).when(faMerchantRestClient)
                .findMerchantId(Mockito.eq("activepan"));
        try {
            MerchantResource resource = faMerchantConnectorService.findMerchant("activepan");
            Assert.assertNotNull(resource);
            BDDMockito.verify(faMerchantRestClient, Mockito.atLeastOnce()).findMerchantId(
                    Mockito.eq("activepan"));
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
        }).when(faMerchantRestClient)
                .findMerchantId(Mockito.any());

        expectedException.expect(Exception.class);
        faMerchantConnectorService.findMerchant("notactivepan");
        BDDMockito.verify(faMerchantRestClient, Mockito.atLeastOnce()).findMerchantId(
                Mockito.eq("notactivepan"));
    }
}