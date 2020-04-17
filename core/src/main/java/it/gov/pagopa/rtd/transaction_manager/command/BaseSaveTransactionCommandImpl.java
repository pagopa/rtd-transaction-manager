package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public abstract class BaseSaveTransactionCommandImpl extends BaseCommand<Boolean> implements SaveTransactionCommand {

    private SaveTransactionCommandModel saveTransactionCommandModel;
    private PaymentInstrumentConnectorService paymentInstrumentConnectorService;
    private PointTransactionPublisherService pointTransactionProducerService;
    private InvoiceTransactionPublisherService invoiceTransactionProducerService;

    public BaseSaveTransactionCommandImpl(SaveTransactionCommandModel saveTransactionCommandModel) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
    }

    public BaseSaveTransactionCommandImpl(
            SaveTransactionCommandModel saveTransactionCommandModel,
            PaymentInstrumentConnectorService paymentInstrumentConnectorService,
            PointTransactionPublisherService pointTransactionProducerService,
            InvoiceTransactionPublisherService invoiceTransactionProducerService) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
        this.pointTransactionProducerService = pointTransactionProducerService;
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
    }

    @Autowired
    public void setPaymentInstrumentConnectorService(
            PaymentInstrumentConnectorService paymentInstrumentConnectorService) {
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
    }

    @Autowired
    public void setPointTransactionProducerService(
            PointTransactionPublisherService pointTransactionProducerService) {
        this.pointTransactionProducerService = pointTransactionProducerService;
    }

    @Autowired
    public void setInvoiceTransactionProducerService(
            InvoiceTransactionPublisherService invoiceTransactionProducerService) {
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
    }

    @SneakyThrows
    @Override
    public Boolean doExecute() {

        Transaction model = saveTransactionCommandModel.getPayload();

        try {

            if (log.isDebugEnabled()) {
                log.debug("Executing SaveTransactionCommand for transaction: " +
                        model.getIdTrxAcquirer() + ", " + model.getAcquirerCode() + ", " + model.getTrxDate());
        }

            if (paymentInstrumentConnectorService.checkActive(model.getHpan(), model.getTrxDate())) {
                //TODO: Distinzione fra BPD ed FA

                if (log.isDebugEnabled()) {
                    log.debug("Publishing valid transaction: " +
                        model.getIdTrxAcquirer() + ", " + model.getAcquirerCode() + ", " + model.getTrxDate());
                }

                pointTransactionProducerService.publishPointTransactionEvent(model);
            }

            return true;

        } catch (Exception e) {
            //TODO: Gestione errori transazione su coda dedicata ed acknowledgment
            if (logger.isErrorEnabled()) {
                logger.error("Error occured during processing for transaction: " +
                        model.getIdTrxAcquirer() + ", " + model.getAcquirerCode() + ", " + model.getTrxDate());
            }
            return false;
        }

    }


}
