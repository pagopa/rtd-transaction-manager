package it.gov.pagopa.rtd.transaction_manager.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.bpd.common.BaseTest;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.TransactionManagerErrorPublisherService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Test class for the SaveTransactionCommand method
 */

public class SaveTransactionCommandTest extends BaseTest {

    @Mock
    PaymentInstrumentConnectorService paymentInstrumentConnectorServiceMock;
    @Mock
    PointTransactionPublisherService pointTransactionProducerServiceMock;
    @Mock
    InvoiceTransactionPublisherService invoiceTransactionProducerServiceMock;
    @Mock
    TransactionManagerErrorPublisherService transactionManagerErrorPublisherServiceMock;

    @Spy
    ObjectMapper objectMapperSpy;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void initTest() {

        Mockito.reset(
                paymentInstrumentConnectorServiceMock,
                pointTransactionProducerServiceMock,
                invoiceTransactionProducerServiceMock,
                transactionManagerErrorPublisherServiceMock);

    }

    @Test
    public void test_BDPActive() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance(transaction);

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
            BDDMockito.verifyZeroInteractions(transactionManagerErrorPublisherServiceMock);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void test_BDPNotActive() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance(transaction);

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
            BDDMockito.verifyZeroInteractions(transactionManagerErrorPublisherServiceMock);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void test_ConnectorKO() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance(transaction);

        try {

            BDDMockito.doThrow(new Exception("Some Exception")).when(paymentInstrumentConnectorServiceMock)
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));

            Boolean isOk = saveTransactionCommand.execute();

            Assert.assertFalse(isOk);
            BDDMockito.verify(paymentInstrumentConnectorServiceMock, Mockito.atLeastOnce())
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.verifyZeroInteractions(pointTransactionProducerServiceMock);
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);
            BDDMockito.verify(transactionManagerErrorPublisherServiceMock)
                    .publishErrorEvent(Mockito.any(), Mockito.any(), Mockito.any());


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void test_PublisherKO() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance(transaction);

        try {

            BDDMockito.doReturn(true).when(paymentInstrumentConnectorServiceMock)
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.doAnswer(invocationOnMock -> {
                throw new Exception("Some Exception");
            }).when(pointTransactionProducerServiceMock)
              .publishPointTransactionEvent(Mockito.any());

            Boolean isOk = saveTransactionCommand.execute();

            Assert.assertFalse(isOk);
            BDDMockito.verify(paymentInstrumentConnectorServiceMock, Mockito.atLeastOnce())
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.verify(pointTransactionProducerServiceMock, Mockito.atLeastOnce())
                    .publishPointTransactionEvent(Mockito.eq(transaction));
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);
            BDDMockito.verify(transactionManagerErrorPublisherServiceMock)
                    .publishErrorEvent(Mockito.any(), Mockito.any(), Mockito.any());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void testExecute_KO_Validation() {

        Transaction transaction = getRequestObject();
        transaction.setAcquirerCode(null);
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance(transaction);

        try {

            BDDMockito.doThrow(new Exception("Some Exception")).when(paymentInstrumentConnectorServiceMock)
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));

            Boolean isOk = saveTransactionCommand.execute();

            Assert.assertFalse(isOk);
            BDDMockito.verify(transactionManagerErrorPublisherServiceMock)
                    .publishErrorEvent(Mockito.any(), Mockito.any(), Mockito.any());
            BDDMockito.verifyZeroInteractions(paymentInstrumentConnectorServiceMock);
            BDDMockito.verifyZeroInteractions(pointTransactionProducerServiceMock);
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void testExecute_KO_Null() {

        SaveTransactionCommand saveTransactionCommand = buildCommandInstance(null);

        try {

            expectedException.expect(AssertionError.class);
            saveTransactionCommand.execute();

            BDDMockito.verifyZeroInteractions(transactionManagerErrorPublisherServiceMock);
            BDDMockito.verifyZeroInteractions(paymentInstrumentConnectorServiceMock);
            BDDMockito.verifyZeroInteractions(pointTransactionProducerServiceMock);
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void testExecute_KO_ErrorProducer() {

        Transaction transaction = getRequestObject();
        SaveTransactionCommand saveTransactionCommand = buildCommandInstance(transaction);

        try {

            BDDMockito.doThrow(new Exception("Some Exception")).when(paymentInstrumentConnectorServiceMock)
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.doAnswer(invocationOnMock -> {
                throw new JsonProcessingException("Some Exception"){};
            }).when(transactionManagerErrorPublisherServiceMock)
                    .publishErrorEvent(Mockito.any(), Mockito.any(), Mockito.any());


            Boolean isOk = saveTransactionCommand.execute();

            Assert.assertFalse(isOk);
            BDDMockito.verify(paymentInstrumentConnectorServiceMock, Mockito.atLeastOnce())
                    .checkActive(Mockito.eq(transaction.getHpan()), Mockito.eq(transaction.getTrxDate()));
            BDDMockito.verifyZeroInteractions(pointTransactionProducerServiceMock);
            BDDMockito.verifyZeroInteractions(invoiceTransactionProducerServiceMock);
            BDDMockito.verify(transactionManagerErrorPublisherServiceMock)
                    .publishErrorEvent(Mockito.any(), Mockito.any(), Mockito.any());


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    protected SaveTransactionCommand buildCommandInstance(Transaction transaction) {
        return new SaveTransactionCommandImpl(
                SaveTransactionCommandModel.builder().payload(transaction).headers(null).build(),
                paymentInstrumentConnectorServiceMock,
                pointTransactionProducerServiceMock,
                invoiceTransactionProducerServiceMock,
                transactionManagerErrorPublisherServiceMock,
                objectMapperSpy
        );
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