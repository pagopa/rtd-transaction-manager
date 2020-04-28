package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.BaseSpringTest;
import eu.sia.meda.async.util.AsyncUtils;
import eu.sia.meda.core.model.ApplicationContext;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Test class for the SaveTransactionCommand method
 */

@ContextConfiguration(classes = SaveTransactionCommandImpl.class)
public class SaveTransactionCommandTest extends BaseSpringTest {

    @MockBean
    PaymentInstrumentConnectorService paymentInstrumentConnectorServiceMock;
    @MockBean
    PointTransactionPublisherService pointTransactionProducerServiceMock;
    @MockBean
    InvoiceTransactionPublisherService invoiceTransactionProducerServiceMock;
    @SpyBean
    ApplicationContext applicationContext;
    @SpyBean
    AsyncUtils asyncUtils;
    @Autowired
    BeanFactory beanFactory;

    @Before
    public void initTest() {

        Mockito.reset(
                paymentInstrumentConnectorServiceMock,
                pointTransactionProducerServiceMock,
                invoiceTransactionProducerServiceMock,
                asyncUtils);

    }

    @Test
    public void test_BDPActive() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance();

        try {

            BDDMockito.doReturn(true).when(paymentInstrumentConnectorServiceMock)
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.doNothing().when(pointTransactionProducerServiceMock)
                    .publishPointTransactionEvent(Mockito.eq(transaction));

            Boolean isOk = saveTransactionCommand.execute();

            Assert.assertTrue(isOk);
            BDDMockito.verify(paymentInstrumentConnectorServiceMock, Mockito.atLeastOnce())
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.verify(pointTransactionProducerServiceMock, Mockito.atLeastOnce())
                    .publishPointTransactionEvent(Mockito.eq(transaction));
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void test_BDPNotActive() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance();

        try {

            BDDMockito.doReturn(false).when(paymentInstrumentConnectorServiceMock)
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.doNothing().when(pointTransactionProducerServiceMock)
                    .publishPointTransactionEvent(Mockito.eq(transaction));

            Boolean isOk = saveTransactionCommand.execute();

            Assert.assertTrue(isOk);
            BDDMockito.verify(paymentInstrumentConnectorServiceMock, Mockito.atLeastOnce())
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.verifyZeroInteractions(pointTransactionProducerServiceMock);
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void test_ConnectorKO() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance();

        try {

            BDDMockito.doThrow(new Exception("Some Exception")).when(paymentInstrumentConnectorServiceMock)
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));

            Boolean isOk = saveTransactionCommand.execute();

            Assert.assertFalse(isOk);
            BDDMockito.verify(paymentInstrumentConnectorServiceMock, Mockito.atLeastOnce())
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.verifyZeroInteractions(pointTransactionProducerServiceMock);
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    protected SaveTransactionCommand buildCommandInstance() {
        return beanFactory.getBean(
                SaveTransactionCommand.class,
                SaveTransactionCommandModel.builder().payload(getRequestObject()).headers(null).build());
    }

    protected Transaction getRequestObject() {
        return Transaction.builder()
                .idTrxAcquirer(1)
                .acquirerCode("001")
                .trxDate(OffsetDateTime.parse("2020-04-09T16:22:45.304Z"))
                .amount(BigDecimal.valueOf(1313.13))
                .operationType("00")
                .hpan("hpan")
                .merchantId(0)
                .circuitType("00")
                .mcc("813")
                .idTrxIssuer(0)
                .amountCurrency("833")
                .correlationId(1)
                .acquirerId(0)
                .build();
    }

}