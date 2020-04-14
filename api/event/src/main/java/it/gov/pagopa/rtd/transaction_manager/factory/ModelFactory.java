package it.gov.pagopa.rtd.transaction_manager.factory;

public interface ModelFactory<T, U> {

    U createModel(T dto);

}
