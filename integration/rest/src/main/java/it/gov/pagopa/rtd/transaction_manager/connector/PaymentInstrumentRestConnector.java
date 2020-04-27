package it.gov.pagopa.rtd.transaction_manager.connector;

import eu.sia.meda.connector.meda.MedaInternalConnector;
import org.springframework.stereotype.Service;

/**
 * @author Alessio Cialini
 * Class extending MedaInternalConnector, used to contact the endpoint for checkActive in the
 * bpd-ms-payment-instrument microservice
 */

@Service
class PaymentInstrumentRestConnector
        extends MedaInternalConnector<Void, Boolean, Void, Boolean> {

}
