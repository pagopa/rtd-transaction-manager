package it.gov.pagopa.rtd.transaction_manager.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.transaction_manager.model.SaveTransactionCommandModel;
import it.gov.pagopa.rtd.transaction_manager.service.InvoiceTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.PaymentInstrumentConnectorService;
import it.gov.pagopa.rtd.transaction_manager.service.PointTransactionPublisherService;
import it.gov.pagopa.rtd.transaction_manager.service.TransactionManagerErrorPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**

 * implementation of the SaveTransactionCommand interface, extends the BaseSaveTransactionCommandImpl class.
 * Defines the proper implementation, it's used in the beanFactory construction
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
class SaveTransactionCommandImpl extends BaseSaveTransactionCommandImpl implements SaveTransactionCommand {

    public SaveTransactionCommandImpl(SaveTransactionCommandModel saveTransactionCommandModel) {
        super(saveTransactionCommandModel);
    }

    public SaveTransactionCommandImpl(
            SaveTransactionCommandModel saveTransactionCommandModel,
            PaymentInstrumentConnectorService paymentInstrumentConnectorService,
            PointTransactionPublisherService pointTransactionProducerService,
            InvoiceTransactionPublisherService invoiceTransactionProducerService,
            TransactionManagerErrorPublisherService transactionManagerErrorPublisherService,
            ObjectMapper objectMapper) {
        super(saveTransactionCommandModel,
              paymentInstrumentConnectorService,
              pointTransactionProducerService,
              invoiceTransactionProducerService,
                transactionManagerErrorPublisherService,
                objectMapper);
    }

    @Override
    public Boolean doExecute() {
        return super.doExecute();
    }

}
