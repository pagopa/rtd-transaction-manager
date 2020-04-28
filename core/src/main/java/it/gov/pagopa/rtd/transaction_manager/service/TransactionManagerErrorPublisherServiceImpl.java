package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.event.BaseEventConnector;
import eu.sia.meda.event.service.BaseErrorPublisherService;
import eu.sia.meda.event.transformer.ErrorEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.rtd.transaction_manager.TransactionManagerErrorPublisherConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the TransactionManagerErrorPublisherService, extends the BaseErrorPublisherService,
 * the connector has the respnsability to send error related messages on the appropriate queue
 */

@Service
class TransactionManagerErrorPublisherServiceImpl
        extends BaseErrorPublisherService
        implements TransactionManagerErrorPublisherService {


    private final TransactionManagerErrorPublisherConnector transactionManagerErrorPublisherConnector;

    @Autowired
    public TransactionManagerErrorPublisherServiceImpl(
            TransactionManagerErrorPublisherConnector transactionManagerErrorPublisherConnector,
            ErrorEventRequestTransformer errorEventRequestTransformer,
            SimpleEventResponseTransformer simpleEventResponseTransformer) {
        super(errorEventRequestTransformer, simpleEventResponseTransformer);
        this.transactionManagerErrorPublisherConnector = transactionManagerErrorPublisherConnector;
    }

    @Override
    protected BaseEventConnector<byte[], Boolean, byte[], Void> getErrorPublisherConnector() {
        return transactionManagerErrorPublisherConnector;
    }


}
