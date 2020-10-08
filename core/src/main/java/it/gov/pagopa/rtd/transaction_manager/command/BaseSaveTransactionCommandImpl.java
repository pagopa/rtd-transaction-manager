package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.rtd.transaction_manager.connector.model.PaymentInstrumentResource;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.validation.*;
import java.util.Set;

/**
 * Base implementation of the SaveTransactionCommandInterface, extending Meda BaseCommand class, the command
 * represents the class interacted with at api level, hiding the multiple calls to the integration connectors
 */

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
abstract class BaseSaveTransactionCommandImpl extends BaseCommand<Boolean> implements SaveTransactionCommand {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    private SaveTransactionCommandModel saveTransactionCommandModel;
    private PaymentInstrumentConnectorService paymentInstrumentConnectorService;
    private FaPaymentInstrumentConnectorService faPaymentInstrumentConnectorService;
    private PointTransactionPublisherService pointTransactionProducerService;
    private InvoiceTransactionPublisherService invoiceTransactionProducerService;

    public BaseSaveTransactionCommandImpl(SaveTransactionCommandModel saveTransactionCommandModel) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
    }

    public BaseSaveTransactionCommandImpl(
            SaveTransactionCommandModel saveTransactionCommandModel,
            PaymentInstrumentConnectorService paymentInstrumentConnectorService,
            FaPaymentInstrumentConnectorService faPaymentInstrumentConnectorService,
            PointTransactionPublisherService pointTransactionProducerService,
            InvoiceTransactionPublisherService invoiceTransactionProducerService) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
        this.faPaymentInstrumentConnectorService = faPaymentInstrumentConnectorService;
        this.pointTransactionProducerService = pointTransactionProducerService;
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
    }

    /**
     * Implementation of the MEDA Command doExecute method, contains the logic for the inbound transaction
     * management, calls the REST endpoint to check if it the related paymentInstrument is active, and eventually
     * sends the Transaction to the proper outbound channel. In case of an error, send a
     * @return boolean to indicate if the command is succesfully executed
     */

    @SneakyThrows
    @Override
    public Boolean doExecute() {

        Transaction transaction = saveTransactionCommandModel.getPayload();

        try {

            log.info("Executing SaveTransactionCommand for transaction: {}, {}, {}",
                        transaction.getIdTrxAcquirer(),
                        transaction.getAcquirerCode(),
                        transaction.getTrxDate());

            validateRequest(transaction);

            try {

                log.info("Calling checkActive for transaction: {}, {}, {}",
                        transaction.getIdTrxAcquirer(),
                        transaction.getAcquirerCode(),
                        transaction.getTrxDate());

                Boolean checkActive = paymentInstrumentConnectorService
                        .checkActive(transaction.getHpan(), transaction.getTrxDate());

                log.info("Called checkActive for transaction: {}, {}, {}",
                        transaction.getIdTrxAcquirer(),
                        transaction.getAcquirerCode(),
                        transaction.getTrxDate());

                if (checkActive) {

                    log.info("Publishing valid transaction on BPD: " +
                            transaction.getIdTrxAcquirer() + ", " +
                            transaction.getAcquirerCode() + ", " +
                            transaction.getTrxDate());

                    pointTransactionProducerService.publishPointTransactionEvent(transaction);

                    log.info("Published valid transaction on BPD: " +
                            transaction.getIdTrxAcquirer() + ", " +
                            transaction.getAcquirerCode() + ", " +
                            transaction.getTrxDate());
                } else {
                    log.info("Met a transaction for an inactive payment instrument on BPD.");
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            log.info("Calling find for transaction on FA " +
                    transaction.getIdTrxAcquirer() + ", " +
                    transaction.getAcquirerCode() + ", " +
                    transaction.getTrxDate());

            PaymentInstrumentResource paymentInstrumentResource =
                    faPaymentInstrumentConnectorService.find(transaction.getHpan());

            log.info("Called find for transaction on FA " +
                    transaction.getIdTrxAcquirer() + ", " +
                    transaction.getAcquirerCode() + ", " +
                    transaction.getTrxDate());

            if (paymentInstrumentResource != null) {

                if ("ACTIVE".equals(paymentInstrumentResource.getStatus()) &&
                    (paymentInstrumentResource.getActivationDate().compareTo(transaction.getTrxDate()) <= 0) &&
                    (paymentInstrumentResource.getDeactivationDate() == null || transaction.getTrxDate()
                           .compareTo(paymentInstrumentResource.getDeactivationDate()) < 0)
                ) {

                    log.info("Publishing valid transaction on BPD: " +
                            transaction.getIdTrxAcquirer() + ", " +
                            transaction.getAcquirerCode() + ", " +
                            transaction.getTrxDate());

                    invoiceTransactionProducerService.publishInvoiceTransactionEvent(transaction);

                    log.info("Published valid transaction on FA: " +
                            transaction.getIdTrxAcquirer() + ", " +
                            transaction.getAcquirerCode() + ", " +
                            transaction.getTrxDate());

                } else {
                    if (log.isInfoEnabled()) {
                        log.info("Met a transaction for an inactive payment instrument on FA.");
                    }
                }

            }

            log.info("Executed SaveTransactionCommand for transaction: {}, {}, {}",
                    transaction.getIdTrxAcquirer(),
                    transaction.getAcquirerCode(),
                    transaction.getTrxDate());

            return true;

        } catch (Exception e) {

            if (transaction != null) {

                if (logger.isErrorEnabled()) {
                    logger.error("Error occured during processing for transaction: " +
                            transaction.getIdTrxAcquirer() + ", " +
                            transaction.getAcquirerCode() + ", " +
                            transaction.getTrxDate());
                    logger.error(e.getMessage(), e);
                }

            }

            throw e;

        }

    }


    @Autowired
    public void setFaPaymentInstrumentConnectorService(
            FaPaymentInstrumentConnectorService faPaymentInstrumentConnectorService) {
        this.faPaymentInstrumentConnectorService = faPaymentInstrumentConnectorService;
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

    /**
     * Method to process a validation check for the parsed Transaction request
     * @param request
     *          instance of Transaction, parsed from the inbound byte[] payload
     * @throws ConstraintViolationException
     */
    private void validateRequest(Transaction request) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(request);
        if (constraintViolations.size() > 0) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

}
