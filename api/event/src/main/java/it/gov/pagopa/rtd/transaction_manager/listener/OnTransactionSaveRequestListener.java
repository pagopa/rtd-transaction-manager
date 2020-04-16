package it.gov.pagopa.rtd.transaction_manager.listener;

import eu.sia.meda.eventlistener.BaseEventListener;
import it.gov.pagopa.rtd.transaction_manager.command.SaveTransactionCommand;
import it.gov.pagopa.rtd.transaction_manager.factory.ModelFactory;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OnTransactionSaveRequestListener extends BaseEventListener {

    private final ModelFactory<Pair<byte[], Headers>, SaveTransactionCommandModel> saveTransactionCommandModelFactory;
    private final BeanFactory beanFactory;

    @Autowired
    public OnTransactionSaveRequestListener(
            ModelFactory<Pair<byte[], Headers>,SaveTransactionCommandModel> saveTransactionCommandModelFactory,
            BeanFactory beanFactory) {
        this.saveTransactionCommandModelFactory = saveTransactionCommandModelFactory;
        this.beanFactory = beanFactory;
    }

    @SneakyThrows
    @Override
    public void onReceived(byte[] payload, Headers headers) {
        try {

            SaveTransactionCommandModel saveTransactionCommandModel =
                    saveTransactionCommandModelFactory.createModel(Pair.of(payload, headers));
            SaveTransactionCommand command = beanFactory.getBean(
                    SaveTransactionCommand.class, saveTransactionCommandModel);
            if (!command.execute()) {
                throw new Exception("Failed to execute SaveTransactionCommand");
            }

        } catch (Exception e) {
            //TODO: Gestione casi d'errori per acknowledgment
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(),e);
            }
            throw e;
        }
    }

}
