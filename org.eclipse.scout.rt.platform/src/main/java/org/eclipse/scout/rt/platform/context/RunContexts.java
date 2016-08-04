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
package org.eclipse.scout.rt.platform.context;

import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Factory methods to create new {@link RunContext} objects to propagate context like the current {@link Subject} and
 * {@link Locale}.
 * <p>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * state among different threads, or allows temporary state changes to be done for the time of executing some code.
 * <p>
 * Usage:
 *
 * <pre>
 * RunContexts.copyCurrent().withLocale(Locale.US).withSubject(...).run(new IRunnable() {
 *
 *   &#064;Override
 *   public void run() throws Exception {
 *      // run code on behalf of the new context
 *   }
 * });
 * </pre>
 *
 * @since 5.1
 * @see RunContext
 */
public final class RunContexts {

  private RunContexts() {
  }

  /**
   * Creates an empty {@link RunContext}.
   * <p>
   * <strong>RunMonitor</strong><br>
   * a new {@link RunMonitor} is created. However, even if there is a current {@link RunMonitor}, it is NOT registered
   * as child monitor, meaning that it will not be cancelled once the current {@link RunMonitor} is cancelled.
   */
  public static RunContext empty() {
    final RunContext runContext = BEANS.get(RunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }

  /**
   * Creates a "snapshot" of the current calling context.<br>
   * <p>
   * <strong>RunMonitor</strong><br>
   * a new {@link RunMonitor} is created, and if the current calling context contains a {@link RunMonitor}, it is also
   * registered within that {@link RunMonitor}. That makes the <i>returned</i> {@link RunContext} to be cancelled as
   * well once the current calling {@link RunContext} is cancelled, but DOES NOT cancel the current calling
   * {@link RunContext} if the <i>returned</i> {@link RunContext} is cancelled.
   */
  public static RunContext copyCurrent() {
    final RunContext runContext = BEANS.get(RunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }
}
