package it.gov.pagopa.rtd.transaction_manager.model;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Model for transaction to be sent in the outbound channel
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idTrxAcquirer", "acquirerCode", "trxDate"}, callSuper = false)
public class Transaction {

    String idTrxAcquirer;

    String acquirerCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    OffsetDateTime trxDate;

    String hpan;

    String operationType;

    String circuitType;

    String idTrxIssuer;

    String correlationId;

    BigDecimal amount;

    String amountCurrency;

    @Size(max = 5)
    String mcc;

    String acquirerId;

    String merchantId;

}
