package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.rtd.transaction_manager.connector.model.MerchantResource;
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
    private FaMerchantConnectorService faMerchantConnectorService;
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
            InvoiceTransactionPublisherService invoiceTransactionProducerService,
            FaMerchantConnectorService faMerchantConnectorService) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
        this.faPaymentInstrumentConnectorService = faPaymentInstrumentConnectorService;
        this.pointTransactionProducerService = pointTransactionProducerService;
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
        this.faMerchantConnectorService = faMerchantConnectorService;
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

            if (log.isDebugEnabled()) {
                log.debug("Executing SaveTransactionCommand for transaction: " +
                        transaction.getIdTrxAcquirer() + ", " +
                        transaction.getAcquirerCode() + ", " +
                        transaction.getTrxDate());
            }

            validateRequest(transaction);

            if (paymentInstrumentConnectorService.checkActive(transaction.getHpan(), transaction.getTrxDate())) {
                if (log.isDebugEnabled()) {
                    log.debug("Publishing valid transaction on BPD: " +
                            transaction.getIdTrxAcquirer() + ", " +
                            transaction.getAcquirerCode() + ", " +
                            transaction.getTrxDate());
                }
                pointTransactionProducerService.publishPointTransactionEvent(transaction);
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Met a transaction for an inactive payment instrument on BPD.");
                }
            }

            PaymentInstrumentResource paymentInstrumentResource =
                    faPaymentInstrumentConnectorService.find(transaction.getHpan());
            if (paymentInstrumentResource != null) {
                MerchantResource merchantResource = faMerchantConnectorService.findMerchant(transaction.getMerchantId());

                if ("ACTIVE".equals(paymentInstrumentResource.getStatus()) &&
                    (paymentInstrumentResource.getActivationDate().compareTo(transaction.getTrxDate()) <= 0) &&
                    (paymentInstrumentResource.getDeactivationDate() == null || transaction.getTrxDate()
                           .compareTo(paymentInstrumentResource.getDeactivationDate()) < 0) &&
                    (merchantResource != null && merchantResource.getTimestampTC()
                            .compareTo(transaction.getTrxDate()) <= 0)
                ) {
                    if (log.isDebugEnabled()) {
                        log.debug("publishing valid transaction on FA: " +
                                transaction.getIdTrxAcquirer() + ", " +
                                transaction.getAcquirerCode() + ", " +
                                transaction.getTrxDate());
                    }
                    invoiceTransactionProducerService.publishInvoiceTransactionEvent(transaction);
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("Met a transaction for an inactive payment instrument on FA.");
                    }
                }

            }

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
    public void setFaMerchantConnectorService(
            FaMerchantConnectorService faMerchantConnectorService) {
        this.faMerchantConnectorService = faMerchantConnectorService;
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
