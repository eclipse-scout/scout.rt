package org.eclipse.scout.rt.rest.jersey.server;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionCommitProtocol;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;

import java.util.Optional;

/**
 * Ensures any ongoing transaction is either committed or rolled back BEFORE the response is sent to the client.
 */
@Order(1e30)
public class TransactionHandlingRestContainerResponseFilter implements IRestContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    Optional.ofNullable(ITransaction.CURRENT.get()).ifPresent(BEANS.get(ITransactionCommitProtocol.class)::commitOrRollback);
  }
}
