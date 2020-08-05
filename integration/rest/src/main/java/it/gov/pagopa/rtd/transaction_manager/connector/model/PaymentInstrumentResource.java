package it.gov.pagopa.rtd.transaction_manager.connector.model;

import lombok.Data;

@Data
public class PaymentInstrumentResource {

    private Status Status;

    public enum Status {
        ACTIVE, INACTIVE
    }

}
