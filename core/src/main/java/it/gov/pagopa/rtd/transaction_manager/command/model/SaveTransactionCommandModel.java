package it.gov.pagopa.rtd.transaction_manager.command.model;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.header.Headers;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveTransactionCommandModel {

    private Transaction payload;
    private Headers headers;

}
