package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.rtd.transaction_manager.connector.model.PaymentInstrumentResource;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;
import it.gov.pagopa.rtd.transaction_manager.service.FaPaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.validation.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private Boolean enableBPD;
    private Boolean enableFA;


    public BaseSaveTransactionCommandImpl(SaveTransactionCommandModel saveTransactionCommandModel,
                                          Boolean enableBPD, Boolean enableFA) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.enableBPD = enableBPD;
        this.enableFA = enableFA;
    }

    public BaseSaveTransactionCommandImpl(
            SaveTransactionCommandModel saveTransactionCommandModel,
            Boolean enableBPD, Boolean enableFA,
            PaymentInstrumentConnectorService paymentInstrumentConnectorService,
            FaPaymentInstrumentConnectorService faPaymentInstrumentConnectorService,
            PointTransactionPublisherService pointTransactionProducerService,
            InvoiceTransactionPublisherService invoiceTransactionProducerService) {
        this.saveTransactionCommandModel = saveTransactionCommandModel;
        this.paymentInstrumentConnectorService = paymentInstrumentConnectorService;
        this.faPaymentInstrumentConnectorService = faPaymentInstrumentConnectorService;
        this.pointTransactionProducerService = pointTransactionProducerService;
        this.invoiceTransactionProducerService = invoiceTransactionProducerService;
        this.enableBPD = enableBPD;
        this.enableFA = enableFA;
    }

    /**
     * Implementation of the MEDA Command doExecute method, contains the logic for the inbound transaction
     * management, calls the REST endpoint to check if it the related paymentInstrument is active, and eventually
     * sends the Transaction to the proper outbound channel. In case of an error, send a
     *
     * @return boolean to indicate if the command is succesfully executed
     */

    @SneakyThrows
    @Override
    public Boolean doExecute() {

        Transaction transaction = saveTransactionCommandModel.getPayload();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss.SSSXXXXX");

        try {

            OffsetDateTime exec_start = OffsetDateTime.now();

            validateRequest(transaction);

            if (enableBPD) {

                OffsetDateTime check_start = OffsetDateTime.now();

                Boolean checkActive = paymentInstrumentConnectorService
                            .checkActive(transaction.getHpan(), transaction.getTrxDate());

                OffsetDateTime check_end = OffsetDateTime.now();

                log.info("Executed checkActive for transaction: {}, {}, {} " +
                                "- Started at {}, Ended at {} - Total exec time: {}",
                        transaction.getIdTrxAcquirer(),
                        transaction.getAcquirerCode(),
                        transaction.getTrxDate(),
                        dateTimeFormatter.format(check_start),
                        dateTimeFormatter.format(check_end),
                        ChronoUnit.MILLIS.between(check_start, check_end));


                if (checkActive) {

                    OffsetDateTime pub_start = OffsetDateTime.now();

                    pointTransactionProducerService.publishPointTransactionEvent(transaction);

                    OffsetDateTime pub_end = OffsetDateTime.now();

                    log.info("Executed publishing on BPD for transaction: {}, {}, {} " +
                                        "- Started at {}, Ended at {} - Total exec time: {}",
                                transaction.getIdTrxAcquirer(),
                                transaction.getAcquirerCode(),
                                transaction.getTrxDate(),
                                dateTimeFormatter.format(pub_start),
                                dateTimeFormatter.format(pub_end),
                                ChronoUnit.MILLIS.between(pub_start, pub_end));

                } else {
                    log.info("Met a transaction for an inactive payment instrument on BPD.");
                }

            }

            if (enableFA) {

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

            }

            OffsetDateTime end_exec = OffsetDateTime.now();

            log.info("Executed SaveTransactionCommand for transaction: {}, {}, {} " +
                            "- Started at {}, Ended at {} - Total exec time: {}",
                    transaction.getIdTrxAcquirer(),
                    transaction.getAcquirerCode(),
                    transaction.getTrxDate(),
                    dateTimeFormatter.format(exec_start),
                    dateTimeFormatter.format(end_exec),
                    ChronoUnit.MILLIS.between(exec_start, end_exec));

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
     *
     * @param request instance of Transaction, parsed from the inbound byte[] payload
     * @throws ConstraintViolationException
     */
    private void validateRequest(Transaction request) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(request);
        if (constraintViolations.size() > 0) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

}
