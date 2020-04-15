package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
class SaveTransactionCommandImpl extends BaseCommand<Transaction> implements SaveTransactionCommand {

    private final SaveTransactionCommandModel saveTransactionCommandModel;
    private final PaymentInstrumentConnectorService paymentInstrumentConnectorService;
    private final PointTransactionPublisherService pointTransactionProducerService;
    private final InvoiceTransactionPublisherService invoiceTransactionProducerService;

    public SaveTransactionCommandImpl(
            SaveTransactionCommandModel saveTransactionCommandModel,
            PaymentInstrumentConnectorService paymentInstrumentConnectorService,
            PointTransactionPublisherService pointTransactionProducerService,
            InvoiceTransactionPublisherService invoiceTransactionProducerService) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
        this.pointTransactionProducerService = pointTransactionProducerService;
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
    }

    @Override
    public Transaction doExecute() {

        Transaction model = saveTransactionCommandModel.getPayload();

        try {

            if (paymentInstrumentConnectorService.checkActive(model.getHpan(), model.getTrxDate())) {
                //TODO: Distinzione fra BPD ed FA
                Supplier<Boolean> lazyValue = () -> {
                    pointTransactionProducerService.publishPointTransactionEvent(model);
                    return true;
                };
                callAsyncService(lazyValue);
            }

            return model;

        } catch (Exception e) {
            //TODO: Gestione errori transazione su coda dedicata
            if (logger.isErrorEnabled()) {
                logger.error("Error occured during processing for transaction: " +
                        model.getIdTrxAcquirer() + ", " + model.getAcquirerCode() + ", " + model.getTrxDate());
            }
        }

        return null;
    }

}
