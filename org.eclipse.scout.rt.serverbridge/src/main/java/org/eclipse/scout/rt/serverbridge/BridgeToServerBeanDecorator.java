/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.serverbridge;

import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.client.IClientNode;
import org.eclipse.scout.rt.client.clientnotification.ClientNotificationDispatcher;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContextIdentifiers;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.clientnotification.TransactionalClientNotificationCollector;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * Bean decorator that executes bean method invocations in a server run context.<br>
 * The decision if a bean already runs on the server is done using context identifiers.
 *
 * @see RunContextIdentifiers
 * @see ServerRunContext
 * @see BridgeToServerBeanDecorationFactory
 * @since 5.2
 */
public class BridgeToServerBeanDecorator<T> implements IBeanDecorator<T> {
  private final IBeanDecorator<T> m_inner;

  public BridgeToServerBeanDecorator() {
    this(null);
  }

  public BridgeToServerBeanDecorator(IBeanDecorator<T> inner) {
    m_inner = inner;
  }

  @Override
  public Object invoke(IBeanInvocationContext<T> context) {
    return ensureRunInServerContext(context);
  }

  protected Object continueCall(IBeanInvocationContext<T> context) {
    IBeanDecorator<T> nextInterceptor = m_inner;
    if (nextInterceptor == null) {
      return context.proceed();
    }
    return nextInterceptor.invoke(context);
  }

  protected Object ensureRunInServerContext(final IBeanInvocationContext<T> context) {
    if (RunContextIdentifiers.isCurrent(ServerRunContext.SERVER_RUN_CONTEXT_IDENTIFIER)) {
      // already in a server scope
      return continueCall(context);
    }

    // bridge to server scope
    TransactionalClientNotificationCollector collector = new TransactionalClientNotificationCollector();
    ServerRunContext bridgeRunContext = ServerRunContexts
        .copyCurrent()
        .withTransactionalClientNotificationCollector(collector)
        .withClientNodeId(IClientNode.ID);
    ISession currentSession = ISession.CURRENT.get();
    IServerSession bridgeSession = null;
    if (currentSession != null) {
      bridgeSession = BEANS.get(ServerSessionProviderWithCache.class).provide(bridgeRunContext, currentSession.getId());
    }
    Object result = bridgeRunContext.withSession(bridgeSession).call(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        try {
          return continueCall(context);
        }
        catch (Exception e) {
          ITransaction.CURRENT.get().addFailure(e);
          throw e;
        }
      }
    });

    ClientNotificationDispatcher clientNotificationDispatcher = BEANS.get(ClientNotificationDispatcher.class);
    List<ClientNotificationMessage> values = collector.consume();
    if (!values.isEmpty()) {
      clientNotificationDispatcher.dispatchNotifications(values);
    }

    return result;
  }
}
