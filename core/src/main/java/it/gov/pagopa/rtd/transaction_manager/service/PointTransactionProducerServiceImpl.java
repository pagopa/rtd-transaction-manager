package it.gov.pagopa.rtd.transaction_manager.service;

import it.gov.pagopa.rtd.transaction_manager.PointTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointTransactionProducerServiceImpl implements PointTransactionProducerService {

    private PointTransactionPublisherService pointTransactionPublisherService;

    @Autowired
    void PointTransactionProducerServiceImpl(PointTransactionPublisherService pointTransactionPublisherService) {
        this.pointTransactionPublisherService = pointTransactionPublisherService;
    }

    @Override
    public void savePointTransaction(Transaction transaction) {
        pointTransactionPublisherService.publishPointTransactionEvent(transaction);
    }
}
