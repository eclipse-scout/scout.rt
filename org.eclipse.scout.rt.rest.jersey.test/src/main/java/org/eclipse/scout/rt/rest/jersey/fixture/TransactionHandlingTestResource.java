package org.eclipse.scout.rt.rest.jersey.fixture;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.rest.IRestResource;

@Path("transactionHandling")
public class TransactionHandlingTestResource implements IRestResource {

  @POST
  @Path("throwExceptionInTransaction")
  @Consumes(MediaType.APPLICATION_JSON)
  public String throwExceptionInTransaction() {
    ITransaction.CURRENT.get().registerMember(new AbstractTransactionMember(TransactionHandlingTestResource.class.getName()) {

      @Override
      public boolean needsCommit() {
        return true;
      }

      @Override
      public boolean commitPhase1() {
        throw new ProcessingException("intentionally failing transaction");
      }
    });
    return "done";
  }
}
