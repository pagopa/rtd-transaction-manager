package it.gov.pagopa.rtd.transaction_manager.service;

import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.rtd.transaction_manager.PointTransactionPublisherConnector;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PointTransactionPublisherServiceImpl implements PointTransactionPublisherService {

    private final PointTransactionPublisherConnector pointTransactionPublisherConnector;
    private final SimpleEventRequestTransformer<Transaction> simpleEventRequestTransformer;
    private final SimpleEventResponseTransformer simpleEventResponseTransformer;

    @Autowired
    public PointTransactionPublisherServiceImpl(PointTransactionPublisherConnector pointTransactionPublisherConnector,
                                         SimpleEventRequestTransformer<Transaction> simpleEventRequestTransformer,
                                         SimpleEventResponseTransformer simpleEventResponseTransformer) {
        this.pointTransactionPublisherConnector = pointTransactionPublisherConnector;
        this.simpleEventRequestTransformer = simpleEventRequestTransformer;
        this.simpleEventResponseTransformer = simpleEventResponseTransformer;
    }

    @Override
    public void publishPointTransactionEvent(Transaction transaction) {
        pointTransactionPublisherConnector.call(
                transaction, simpleEventRequestTransformer, simpleEventResponseTransformer);
    }
}
