package it.gov.pagopa.rtd.transaction_manager;

import eu.sia.meda.event.BaseEventConnector;
import org.springframework.stereotype.Service;

/**
 * Class extending the MEDA BaseEventConnector, is responsible for calling a Kafka outbound channel with messages
 * containing content in byte[] format class
 */

@Service
public class TransactionManagerErrorPublisherConnector
        extends BaseEventConnector<byte[], Boolean, byte[], Void> {

}
