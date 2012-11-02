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
package org.eclipse.scout.jaxws.handler.internal;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.RunnableWithException;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.internal.ScoutTransactionDelegate;

public class ScoutTransactionSOAPHandlerWrapper<T extends SOAPMessageContext> implements SOAPHandler<T>, IScoutTransactionHandlerWrapper<T> {

  protected final ScoutTransactionDelegate m_transactionDelegate;
  protected final SOAPHandler<T> m_soapHandler;

  public ScoutTransactionSOAPHandlerWrapper(SOAPHandler<T> soapHandler, ScoutTransaction scoutTransaction) {
    m_transactionDelegate = createTransactionDelegate(scoutTransaction);
    m_soapHandler = soapHandler;
  }

  @Override
  public boolean handleMessage(final T context) {
    RunnableWithException<Boolean> runnable = new RunnableWithException<Boolean>() {

      @Override
      public Boolean run() throws Throwable {
        return m_soapHandler.handleMessage(context);
      }
    };
    return m_transactionDelegate.runInTransaction(runnable, context);
  }

  @Override
  public boolean handleFault(final T context) {
    RunnableWithException<Boolean> runnable = new RunnableWithException<Boolean>() {

      @Override
      public Boolean run() throws Throwable {
        return m_soapHandler.handleFault(context);
      }
    };
    return m_transactionDelegate.runInTransaction(runnable, context);
  }

  @Override
  public void close(final MessageContext context) {
    RunnableWithException<Boolean> runnable = new RunnableWithException<Boolean>() {

      @Override
      public Boolean run() throws Throwable {
        m_soapHandler.close(context);
        return null;
      }
    };
    m_transactionDelegate.runInTransaction(runnable, context);
  }

  @Override
  public Set<QName> getHeaders() {
    return m_soapHandler.getHeaders();
  }

  protected ScoutTransactionDelegate createTransactionDelegate(ScoutTransaction scoutTransaction) {
    return new ScoutTransactionDelegate(scoutTransaction);
  }

  @Override
  public Handler<T> getHandler() {
    return m_soapHandler;
  }
}
