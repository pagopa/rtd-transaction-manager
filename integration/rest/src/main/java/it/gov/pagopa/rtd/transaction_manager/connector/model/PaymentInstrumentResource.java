package it.gov.pagopa.rtd.transaction_manager.connector.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PaymentInstrumentResource {

    private String status;
    private OffsetDateTime activationDate;
    private OffsetDateTime deactivationDate;

}
