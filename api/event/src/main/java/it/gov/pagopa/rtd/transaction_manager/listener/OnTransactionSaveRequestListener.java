package it.gov.pagopa.rtd.transaction_manager.listener;

import eu.sia.meda.eventlistener.BaseEventListener;
import it.gov.pagopa.rtd.transaction_manager.command.SaveTransactionCommand;
import it.gov.pagopa.rtd.transaction_manager.command.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.factory.ModelFactory;
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

    private final ModelFactory<Pair<byte[], Headers>,SaveTransactionCommandModel> saveTransactionCommandModelFactory;
    private final BeanFactory beanFactory;

    @Autowired
    public OnTransactionSaveRequestListener(
            ModelFactory<Pair<byte[], Headers>,SaveTransactionCommandModel> saveTransactionCommandModelFactory,
            BeanFactory beanFactory) {
        this.saveTransactionCommandModelFactory = saveTransactionCommandModelFactory;
        this.beanFactory = beanFactory;
    }

    @Override
    public void onReceived(byte[] payload, Headers headers) {
        try {

            SaveTransactionCommandModel saveTransactionCommandModel =
                    saveTransactionCommandModelFactory.createModel(Pair.of(payload, headers));
            SaveTransactionCommand command = beanFactory.getBean(SaveTransactionCommand.class,
                    saveTransactionCommandModel);
            command.execute();

        } catch (Exception e){
            String payloadString = "null";
            if(payload != null){
                try{
                    payloadString = new String(payload, StandardCharsets.UTF_8);
                } catch (Exception e2){
                    if (log.isErrorEnabled()) {
                        log.error("Something gone wrong converting the payload into String", e2);
                    }
                }
            }
            if (log.isErrorEnabled()) {
                log.error(String.format(
                        "Something gone wrong during the evaluation of the payload:%n%s", payloadString), e);
            }
        }
    }



}
