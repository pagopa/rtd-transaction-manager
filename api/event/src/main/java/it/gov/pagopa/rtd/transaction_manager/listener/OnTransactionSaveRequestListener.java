package it.gov.pagopa.rtd.transaction_manager.listener;

import eu.sia.meda.eventlistener.BaseEventListener;
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
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class OnTransactionSaveRequestListener extends BaseEventListener {

    private final TransactionManagerErrorPublisherService transactionManagerErrorPublisherService;
    private final ModelFactory<Pair<byte[], Headers>, SaveTransactionCommandModel> saveTransactionCommandModelFactory;
    private final BeanFactory beanFactory;

    @Autowired
    public OnTransactionSaveRequestListener(
            TransactionManagerErrorPublisherService transactionManagerErrorPublisherService,
            ModelFactory<Pair<byte[], Headers>,SaveTransactionCommandModel> saveTransactionCommandModelFactory,
            BeanFactory beanFactory) {
        this.transactionManagerErrorPublisherService = transactionManagerErrorPublisherService;
        this.saveTransactionCommandModelFactory = saveTransactionCommandModelFactory;
        this.beanFactory = beanFactory;
    }

    @SneakyThrows
    @Override
    public void onReceived(byte[] payload, Headers headers) {

        try {

            if (log.isDebugEnabled()) {
                log.debug("Processing new request on inbound queue");
            }

            SaveTransactionCommandModel saveTransactionCommandModel =
                    saveTransactionCommandModelFactory.createModel(Pair.of(payload, headers));
            SaveTransactionCommand command = beanFactory.getBean(
                    SaveTransactionCommand.class, saveTransactionCommandModel);
            if (!command.execute()) {
                throw new Exception("Failed to execute SaveTransactionCommand");
            }

        } catch (Exception e) {
            //TODO: Gestione casi d'errori per acknowledgment
            String payloadString = "null";
            if (payload != null) {
                try {
                    payloadString = new String(payload, StandardCharsets.UTF_8);
                } catch (Exception e2) {
                    logger.error("Something gone wrong converting the payload into String", e2);
                }
                logger.error(String.format(
                        "Something gone wrong during the evaluation of the payload:%n%s", payloadString), e);
            }
            if (!transactionManagerErrorPublisherService.publishErrorEvent(payload, headers,
                    "Unexpected error during transaction processing")) {
                if (log.isErrorEnabled()) {
                    log.error("Could not publish transaction processing error");
                }
            }
            throw e;
        }
    }

}
