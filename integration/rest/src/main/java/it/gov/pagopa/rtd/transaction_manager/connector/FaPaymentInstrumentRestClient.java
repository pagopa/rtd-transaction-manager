package it.gov.pagopa.rtd.transaction_manager.connector;

import it.gov.pagopa.rtd.transaction_manager.connector.model.PaymentInstrumentResource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotBlank;

/**
 * FaPayment Instrument Rest Client
 */
@FeignClient(name = "${rest-client.fa-payment-instrument.serviceCode}", url = "${rest-client.fa-payment-instrument.base-url}")
public interface FaPaymentInstrumentRestClient {

    @GetMapping(value = "${rest-client.fa-payment-instrument.find.url}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    PaymentInstrumentResource find(
            @PathVariable @NotBlank String hpan
    );
}
