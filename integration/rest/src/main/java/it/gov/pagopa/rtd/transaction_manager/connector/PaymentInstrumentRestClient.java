package it.gov.pagopa.rtd.transaction_manager.connector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

/**
 * Payment Instrument Rest Client
 */
@FeignClient(name = "${rest-client.payment-instrument.serviceCode}", url = "${rest-client.payment-instrument.base-url}")
public interface PaymentInstrumentRestClient {

    @GetMapping(value = "/{id}/history/active", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    boolean checkActive(
            @PathVariable("id")
            @NotBlank
                    String hpan,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime accountingDate);
}
