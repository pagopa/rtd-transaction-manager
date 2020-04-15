package it.gov.pagopa.rtd.transaction_manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.BaseSpringTest;
import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.rtd.transaction_manager.InvoiceTransactionPublisherConnector;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@ContextConfiguration(classes = InvoiceTransactionPublisherServiceImpl.class)
public class InvoiceTransactionPublisherServiceTest extends BaseSpringTest {

    @SpyBean
    ObjectMapper objectMapper;

    @MockBean
    private InvoiceTransactionPublisherConnector invoiceTransactionPublisherConnectorMock;

    @SpyBean
    private SimpleEventRequestTransformer<Transaction> simpleEventRequestTransformerSpy;

    @SpyBean
    private SimpleEventResponseTransformer simpleEventResponseTransformerSpy;

    @Autowired
    InvoiceTransactionPublisherService invoiceTransactionPublisherService;

    private Transaction transaction;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(
                invoiceTransactionPublisherConnectorMock,
                simpleEventRequestTransformerSpy,
                simpleEventResponseTransformerSpy);

        transaction = getRequestObject();

        BDDMockito.doReturn(true)
                .when(invoiceTransactionPublisherConnectorMock)
                .doCall(Mockito.eq(transaction),
                      Mockito.eq(simpleEventRequestTransformerSpy),
                      Mockito.eq(simpleEventResponseTransformerSpy),
                        Mockito.any());


    }

    @Test
    public void publishInvoiceTransactionEvent() {
        try {
            invoiceTransactionPublisherService
                    .publishInvoiceTransactionEvent(transaction);
            BDDMockito.verify(invoiceTransactionPublisherConnectorMock,Mockito.atLeastOnce())
                    .doCall(Mockito.eq(transaction),
                            Mockito.eq(simpleEventRequestTransformerSpy),
                            Mockito.eq(simpleEventResponseTransformerSpy));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
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
                .awardPeriodId(0L)
                .acquirerId(0)
                .build();
    }

}