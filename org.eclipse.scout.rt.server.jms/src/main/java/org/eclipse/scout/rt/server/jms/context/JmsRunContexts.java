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
package org.eclipse.scout.rt.server.jms.context;

import javax.jms.Message;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;

/**
 * Factory methods to create new {@link JmsRunContext} objects to propagate <i>JMS Java Message Service</i> state like
 * {@link Message}.
 * <p>
 * Usage:
 *
 * <pre>
 * JmsRunContexts.copyCurrent().withLocale(Locale.US).withMessage(...).run(new IRunnable() {
 *
 *   &#064;Override
 *   public void run() throws Exception {
 *      // run code on behalf of the new context
 *   }
 * });
 * </pre>
 *
 * @since 5.1
 * @see JmsRunContext
 */
public final class JmsRunContexts {

  private JmsRunContexts() {
  }

  /**
   * Creates an empty {@link JmsRunContext} with all values managed by {@link JmsRunContext} class set to their default
   * value. This method does not require to already run in a {@link RunContext}.
   * <p>
   * {@link RunMonitor}<br>
   * Uses a new {@link RunMonitor} which is not registered as child monitor of {@link RunMonitor#CURRENT}, meaning that
   * the context is not cancelled upon cancellation of the current monitor.
   * <p>
   * {@link TransactionScope}<br>
   * Uses the transaction scope {@link TransactionScope#REQUIRED} which starts a new transaction only if not running in
   * a transaction yet.
   */
  public static JmsRunContext empty() {
    return BEANS.get(JmsRunContextFactory.class).empty();
  }

  /**
   * Factory to create initialized {@link JmsRunContext} objects.
   */
  @ApplicationScoped
  public static class JmsRunContextFactory extends RunContextFactory {

    @Override
    public JmsRunContext copyCurrent() {
      return (JmsRunContext) super.copyCurrent();
    }

    @Override
    public JmsRunContext empty() {
      return (JmsRunContext) super.empty();
    }

    @Override
    protected JmsRunContext newInstance() {
      return BEANS.get(JmsRunContext.class);
    }
  }
}
