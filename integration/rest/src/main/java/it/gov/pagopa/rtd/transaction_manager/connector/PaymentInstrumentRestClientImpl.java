package it.gov.pagopa.rtd.transaction_manager.connector;

import eu.sia.meda.connector.rest.transformer.request.SimpleRestGetRequestTransformer;
import eu.sia.meda.connector.rest.transformer.response.SimpleRest2xxResponseTransformer;
import eu.sia.meda.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;

/**
 * @author Alessio Cialini
 * Implementation of the PaymentInstrumentRestClient interface, the class contains the logic for the creation
 * of the query to pass at the connector call
 */

@Service
class PaymentInstrumentRestClientImpl extends BaseService implements PaymentInstrumentRestClient{

    private final PaymentInstrumentRestConnector connector;
    private final SimpleRestGetRequestTransformer requestTransformer;
    private final SimpleRest2xxResponseTransformer<Boolean> responseTransformer;

    @Autowired
    public PaymentInstrumentRestClientImpl(PaymentInstrumentRestConnector connector,
                                SimpleRestGetRequestTransformer requestTransformer,
                                SimpleRest2xxResponseTransformer<Boolean> responseTransformer) {
        this.connector = connector;
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    /**
     * Method to execute a call at the REST enpoint regarding the request of the status of an intrument,
     * identified from an hpan, and given a particular accountingDate
     * @param hpan
     *          hpan to identify a particular payment instrument
     * @param accountingDate
     *          date on which the payment instrument should be active
     * @return a boolean in response, defining if the payment instrument results aactive or not
     */
    @Override
    public Boolean checkActive(String hpan, OffsetDateTime accountingDate) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("id", hpan);
        final HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("accountingDate", accountingDate.toString());
        return connector.call(null, requestTransformer, responseTransformer, params, queryParams);
    }
}
