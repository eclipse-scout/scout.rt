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
package org.eclipse.scout.rt.platform.context;

import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.OBJ;

/**
 * Factory methods to create new {@link RunContext} objects to propagate context like the current {@link Subject} and
 * {@link Locale}.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * state among different threads, or allows temporary state changes to be done for the time of executing some code.
 * <p/>
 * Usage:
 *
 * <pre>
 * RunContexts.copyCurrent().locale(Locale.US).subject(...).run(new IRunnable() {
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
   * Creates an empty {@link RunContext} with <code>null</code> as preferred {@link Subject} and {@link Locale}.
   * Preferred means, that those values will not be derived from other values, but must be set explicitly instead.
   */
  public static final RunContext empty() {
    final RunContext runContext = OBJ.get(RunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }

  /**
   * Creates a "snapshot" of the current calling context.
   */
  public static RunContext copyCurrent() {
    final RunContext runContext = OBJ.get(RunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }
}
