/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Factory methods to create new {@link JmsRunContext} objects to propagate <i>JMS Java Message Service</i> state like
 * {@link Message}.
 * <p/>
 * Usage:
 *
 * <pre>
 * JmsRunContexts.empty().locale(Locale.US).message(...).run(new IRunnable() {
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
   * Creates an empty <code>JmsRunContext</code>.
   */
  public static final JmsRunContext empty() {
    final JmsRunContext runContext = BEANS.get(JmsRunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }

  /**
   * Creates a "snapshot" of the current calling <code>JmsRunContext</code>.
   */
  public static JmsRunContext copyCurrent() {
    final JmsRunContext runContext = BEANS.get(JmsRunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }
}
