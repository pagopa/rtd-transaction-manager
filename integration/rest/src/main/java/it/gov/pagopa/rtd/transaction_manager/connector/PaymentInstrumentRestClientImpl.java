package it.gov.pagopa.rtd.transaction_manager.connector;

import eu.sia.meda.connector.rest.transformer.request.SimpleRestGetRequestTransformer;
import eu.sia.meda.connector.rest.transformer.response.SimpleRest2xxResponseTransformer;
import eu.sia.meda.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;

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

    @Override
    public Boolean checkActive(String hpan, OffsetDateTime accountingDate) {

        final HashMap<String, Object> params = new HashMap<>();
        params.put("id", hpan);
        final HashMap<String, Object> queryParams = new HashMap<>();
        params.put("accountingDate", accountingDate.toString());

        return connector.call(null, requestTransformer, responseTransformer, params, queryParams);
    }
}
