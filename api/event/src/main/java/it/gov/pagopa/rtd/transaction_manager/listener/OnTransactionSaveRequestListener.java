package it.gov.pagopa.rtd.transaction_manager.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.eventlistener.BaseConsumerAwareEventListener;
import it.gov.pagopa.rtd.transaction_manager.command.SaveTransactionCommand;
import it.gov.pagopa.rtd.transaction_manager.factory.ModelFactory;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.service.TransactionManagerErrorPublisherService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Class Extending the MEDA BaseEventListener, manages the inbound requests, and calls on the appropriate
 * command for the check and send logic associated to the Transaction payload
 */

@Service
@Slf4j
public class OnTransactionSaveRequestListener extends BaseConsumerAwareEventListener {

    private final TransactionManagerErrorPublisherService transactionManagerErrorPublisherService;
    private final ModelFactory<Pair<byte[], Headers>, SaveTransactionCommandModel> saveTransactionCommandModelFactory;
    private final BeanFactory beanFactory;
    private final ObjectMapper objectMapper;

    @Value("${it.gov.pagopa.rtd.transaction_manager.command.bpd.enabled}")
    private Boolean enableBPD;

    @Value("${it.gov.pagopa.rtd.transaction_manager.command.fa.enabled}")
    private Boolean enableFA;

    @Autowired
    public OnTransactionSaveRequestListener(
            TransactionManagerErrorPublisherService transactionManagerErrorPublisherService,
            ModelFactory<Pair<byte[], Headers>, SaveTransactionCommandModel> saveTransactionCommandModelFactory,
            BeanFactory beanFactory,
            ObjectMapper objectMapper) {
        this.transactionManagerErrorPublisherService = transactionManagerErrorPublisherService;
        this.saveTransactionCommandModelFactory = saveTransactionCommandModelFactory;
        this.beanFactory = beanFactory;
        this.objectMapper = objectMapper;
    }

    /**
     * Method called on receiving a message in the inbound queue,
     * that should contain a JSON payload containing transaction data,
     * calls on a command to execute the check and send logic for the input Transaction data
     * In case of error, sends data to an error channel
     * @param payload
     *          Message JSON payload in byte[] format
     * @param headers
     *          Kafka headers from the inbound message
     */

    @SneakyThrows
    @Override
    public void onReceived(byte[] payload, Headers headers) {

        SaveTransactionCommandModel saveTransactionCommandModel = null;

        try {

            if (log.isDebugEnabled()) {
                log.debug("Processing new request on inbound queue");
            }

            saveTransactionCommandModel = saveTransactionCommandModelFactory
                    .createModel(Pair.of(payload, headers));
            SaveTransactionCommand command = beanFactory.getBean(
                    SaveTransactionCommand.class, saveTransactionCommandModel, enableBPD, enableFA);

            if (!command.execute()) {
                throw new Exception("Failed to execute SaveTransactionCommand");
            }

            if (log.isDebugEnabled()) {
                log.debug("SaveTransactionCommand successfully executed for inbound message");
            }

        } catch (Exception e) {

            String payloadString = "null";
            String error = "Unexpected error during transaction processing";

            try {
                payloadString = new String(payload, StandardCharsets.UTF_8);
            } catch (Exception e2) {
                if (logger.isErrorEnabled()) {
                    logger.error("Something gone wrong converting the payload into String", e2);
                }
            }

            if (saveTransactionCommandModel != null && saveTransactionCommandModel.getPayload() != null) {
                payloadString = new String(payload, StandardCharsets.UTF_8);
                error = String.format("Unexpected error during transaction processing: %s, %s",
                        payloadString, e.getMessage());
            } else if (payload != null) {
                error = String.format("Something gone wrong during the evaluation of the payload: %s, %s",
                        payloadString, e.getMessage());
                if (logger.isErrorEnabled()) {
                    logger.error(error, e);
                }
            }

            if (!transactionManagerErrorPublisherService.publishErrorEvent(payload, headers, error)) {
                if (log.isErrorEnabled()) {
                    log.error("Could not publish transaction processing error");
                }
                throw e;
            }

        }
    }

}
