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
package org.eclipse.scout.rt.server.jaxws.provider.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;

/**
 * Factory methods to create new {@link JaxWsServletRunContext} objects to propagate JAX-WS state like
 * {@link WebServiceContext}, {@link HttpServletRequest} and {@link HttpServletResponse}.
 * <p>
 * Usage:
 *
 * <pre>
 * JaxWsServletRunContexts.copyCurrent().withWebServiceContext(...).run(new IRunnable() {
 *
 *   &#064;Override
 *   public void run() throws Exception {
 *      // run code on behalf of the new context
 *   }
 * });
 * </pre>
 *
 * @since 5.1
 * @see JaxWsServletRunContext
 */
public final class JaxWsServletRunContexts {

  private JaxWsServletRunContexts() {
  }

  /**
   * Creates a "snapshot" of the current calling context for values managed by {@link JaxWsServletRunContext} class.
   * <p>
   * {@link RunMonitor}<br>
   * Uses a new {@link RunMonitor} which is registered as child monitor of {@link RunMonitor#CURRENT}, meaning that the
   * context is cancelled upon cancellation of the current (parent) monitor. Cancellation works top-down, so
   * cancellation of the context's monitor has no effect to the current (parent) monitor.
   * <p>
   * {@link TransactionScope}<br>
   * Uses the transaction scope {@link TransactionScope#REQUIRED} which starts a new transaction only if not running in
   * a transaction yet.
   * <p>
   * {@link ITransactionMember}<br>
   * If the current context has some transaction members registered, those are not registered with the new context.
   * <p>
   * {@link ThreadLocal}<br>
   * Thread-Locals associated with the current context via {@link RunContext#withThreadLocal(ThreadLocal, Object)} are
   * copied as well.
   */
  public static JaxWsServletRunContext copyCurrent() {
    if (RunContext.CURRENT.get() == null) {
      return BEANS.get(JaxWsServletRunContextFactory.class).empty();
    }
    return BEANS.get(JaxWsServletRunContextFactory.class).copyCurrent();
  }

  /**
   * Factory to create initialized {@link JaxWsServletRunContext} objects.
   */
  @ApplicationScoped
  public static class JaxWsServletRunContextFactory extends RunContextFactory {

    @Override
    public JaxWsServletRunContext copyCurrent() {
      return (JaxWsServletRunContext) super.copyCurrent();
    }

    @Override
    public JaxWsServletRunContext empty() {
      return (JaxWsServletRunContext) super.empty();
    }

    @Override
    protected JaxWsServletRunContext newInstance() {
      return BEANS.get(JaxWsServletRunContext.class);
    }
  }
}
