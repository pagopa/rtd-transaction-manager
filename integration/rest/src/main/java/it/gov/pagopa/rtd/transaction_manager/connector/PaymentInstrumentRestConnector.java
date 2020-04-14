package it.gov.pagopa.rtd.transaction_manager.connector;

import eu.sia.meda.connector.meda.MedaInternalConnector;
import org.springframework.stereotype.Service;


@Service
class PaymentInstrumentRestConnector
        extends MedaInternalConnector<Void, Boolean, Void, Boolean> {

}
