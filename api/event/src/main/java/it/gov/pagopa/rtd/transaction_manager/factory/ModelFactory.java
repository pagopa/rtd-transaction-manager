package it.gov.pagopa.rtd.transaction_manager.factory;

/**
 * interface to be used for inheritance for model factories from a DTO
 */

public interface ModelFactory<T, U> {

    U createModel(T dto);

}
