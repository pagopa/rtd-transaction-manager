package it.gov.pagopa.rtd.transaction_manager.connector;

import io.swagger.annotations.ApiParam;
import it.gov.pagopa.rtd.transaction_manager.connector.model.MerchantResource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotBlank;

@FeignClient(name = "${rest-client.fa-merchant.serviceCode}", url = "${rest-client.fa-merchant.base-url}")
public interface FaMerchantRestClient {

    @GetMapping(value = "${rest-client.fa-merchant.find.url}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    MerchantResource findMerchantId(@ApiParam(value = "${swagger.merchant.shopId}", required = true)
                                    @PathVariable @NotBlank String shopId);

}
