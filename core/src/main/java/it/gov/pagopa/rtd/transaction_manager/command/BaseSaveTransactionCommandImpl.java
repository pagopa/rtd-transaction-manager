package it.gov.pagopa.rtd.transaction_manager.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.TransactionManagerErrorPublisherService;
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
    private PointTransactionPublisherService pointTransactionProducerService;
    private InvoiceTransactionPublisherService invoiceTransactionProducerService;
    private TransactionManagerErrorPublisherService transactionManagerErrorPublisherService;
    private ObjectMapper objectMapper;

    public BaseSaveTransactionCommandImpl(SaveTransactionCommandModel saveTransactionCommandModel) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
    }

    public BaseSaveTransactionCommandImpl(
            SaveTransactionCommandModel saveTransactionCommandModel,
            PaymentInstrumentConnectorService paymentInstrumentConnectorService,
            PointTransactionPublisherService pointTransactionProducerService,
            InvoiceTransactionPublisherService invoiceTransactionProducerService,
            TransactionManagerErrorPublisherService transactionManagerErrorPublisherService,
            ObjectMapper objectMapper) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
        this.pointTransactionProducerService = pointTransactionProducerService;
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
        this.transactionManagerErrorPublisherService = transactionManagerErrorPublisherService;
        this.objectMapper = objectMapper;
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
                //TODO: Distinzione fra BPD ed FA
                if (log.isDebugEnabled()) {
                    log.debug("Publishing valid transaction");
                }
                pointTransactionProducerService.publishPointTransactionEvent(transaction);
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Met a transaction for an unactive payment instrument. Discarding.");
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
                }
                try {
                    transactionManagerErrorPublisherService.publishErrorEvent(
                            objectMapper.writeValueAsBytes(transaction),
                            saveTransactionCommandModel.getHeaders(),
                            "Error occured during processing for transaction:" + e.getMessage());
                } catch (JsonProcessingException ex) {
                    if (logger.isErrorEnabled()) {
                        logger.error(e.getMessage(), e);
                    }
                }

                return false;
            }

            throw e;

        }

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

    @Autowired
    public void setTransactionManagerErrorPublisherService(
            TransactionManagerErrorPublisherService transactionManagerErrorPublisherService) {
        this.transactionManagerErrorPublisherService = transactionManagerErrorPublisherService;
    }

    @Autowired
    public void setObjectMapper(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
