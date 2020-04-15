package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.BaseSpringTest;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@ContextConfiguration(classes = SaveTransactionCommandImpl.class)
public class SaveTransactionCommandTest extends BaseSpringTest {

    @Autowired
    BeanFactory beanFactory;

    @Before
    public void initTest() {
    }

    @Test
    public void testOk() {

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