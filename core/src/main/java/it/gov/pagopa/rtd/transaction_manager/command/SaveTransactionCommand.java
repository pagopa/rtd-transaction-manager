package it.gov.pagopa.rtd.transaction_manager.command;

import eu.sia.meda.core.command.Command;
import it.gov.pagopa.rtd.transaction_manager.model.Transaction;

public interface SaveTransactionCommand extends Command<Transaction> {
}
