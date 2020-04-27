package it.gov.pagopa.rtd.transaction_manager.factory;

/**
 * @author ALessio Cialini
 * interface to be used for inheritance for model factories from a DTO
 */

public interface ModelFactory<T, U> {

    U createModel(T dto);

}
