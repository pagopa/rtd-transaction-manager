package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.rtd.transaction_manager.command.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionProducerService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.function.Supplier;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
class SaveTransactionCommandImpl extends BaseCommand<Transaction> implements SaveTransactionCommand {

    private final SaveTransactionCommandModel saveTransactionCommandModel;
    private final PaymentInstrumentConnectorService paymentInstrumentConnectorService;
    private final PointTransactionProducerService pointTransactionProducerService;
    private final InvoiceTransactionProducerService invoiceTransactionProducerService;

    public SaveTransactionCommandImpl(
            SaveTransactionCommandModel saveTransactionCommandModel,
            PaymentInstrumentConnectorService paymentInstrumentConnectorService,
            PointTransactionProducerService pointTransactionProducerService,
            InvoiceTransactionProducerService invoiceTransactionProducerService) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
        this.pointTransactionProducerService = pointTransactionProducerService;
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
    }

    @Override
    public Transaction doExecute() {

        try {

            Transaction model = saveTransactionCommandModel.getPayload();

            if (paymentInstrumentConnectorService.checkActive(model.getHpan(), OffsetDateTime.now())) {
                //TODO: Distinzione fra BPD ed FA
                Supplier<Boolean> lazyValue = () -> {
                    pointTransactionProducerService.savePointTransaction(model);
                    return true;
                };
                callAsyncService(lazyValue);
            }

            return model;

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error occured during processing for transaction: ");
            }
        }

        return null;
    }

}
