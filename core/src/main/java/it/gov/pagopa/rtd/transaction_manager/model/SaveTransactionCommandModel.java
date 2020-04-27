package it.gov.pagopa.rtd.transaction_manager.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.header.Headers;

/**
 * @author Alessio Cialini
 *  Model containing the inbound message data
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveTransactionCommandModel {

    private Transaction payload;
    private Headers headers;

}
