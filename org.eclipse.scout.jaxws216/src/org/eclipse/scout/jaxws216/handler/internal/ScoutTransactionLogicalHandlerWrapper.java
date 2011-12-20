/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws216.handler.internal;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.commons.RunnableWithException;
import org.eclipse.scout.jaxws216.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws216.internal.ScoutTransactionDelegate;

public class ScoutTransactionLogicalHandlerWrapper<T extends LogicalMessageContext> implements LogicalHandler<T>, IScoutTransactionHandlerWrapper<T> {

  protected final ScoutTransactionDelegate m_transactionDelegate;
  protected final LogicalHandler<T> m_logicalHandler;

  public ScoutTransactionLogicalHandlerWrapper(LogicalHandler<T> logicalHandler, ScoutTransaction scoutTransaction) {
    m_transactionDelegate = createTransactionDelegate(scoutTransaction);
    m_logicalHandler = logicalHandler;
  }

  @Override
  public boolean handleMessage(final T context) {
    RunnableWithException<Boolean> runnable = new RunnableWithException<Boolean>() {

      @Override
      public Boolean run() throws Throwable {
        return m_logicalHandler.handleMessage(context);
      }
    };
    return m_transactionDelegate.runInTransaction(runnable, context);
  }

  @Override
  public boolean handleFault(final T context) {
    RunnableWithException<Boolean> runnable = new RunnableWithException<Boolean>() {

      @Override
      public Boolean run() throws Throwable {
        return m_logicalHandler.handleFault(context);
      }
    };
    return m_transactionDelegate.runInTransaction(runnable, context);
  }

  @Override
  public void close(final MessageContext context) {
    RunnableWithException<Boolean> runnable = new RunnableWithException<Boolean>() {

      @Override
      public Boolean run() throws Throwable {
        m_logicalHandler.close(context);
        return null;
      }
    };
    m_transactionDelegate.runInTransaction(runnable, context);
  }

  protected ScoutTransactionDelegate createTransactionDelegate(ScoutTransaction scoutTransaction) {
    return new ScoutTransactionDelegate(scoutTransaction);
  }

  @Override
  public Handler<T> getHandler() {
    return null;
  }
}
