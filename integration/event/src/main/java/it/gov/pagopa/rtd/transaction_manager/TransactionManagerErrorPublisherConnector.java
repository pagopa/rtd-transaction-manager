package it.gov.pagopa.rtd.transaction_manager;

import eu.sia.meda.event.BaseEventConnector;
import org.springframework.stereotype.Service;

@Service
public class TransactionManagerErrorPublisherConnector
        extends BaseEventConnector<byte[], Boolean, byte[], Void> {

}
