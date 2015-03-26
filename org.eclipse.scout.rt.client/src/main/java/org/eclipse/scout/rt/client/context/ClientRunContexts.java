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
package org.eclipse.scout.rt.client.context;

import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Factory methods to create new {@link ClientRunContext} objects to propagate client-side context.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * client state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p/>
 * Usage:
 *
 * <pre>
 * ClientRunContexts.copyCurrent().locale(Locale.US).subject(...).session(...).run(new IRunnable() {
 * 
 *   &#064;Override
 *   public void run() throws Exception {
 *      // run code on behalf of the new context
 *   }
 * });
 * </pre>
 *
 * @since 5.1
 * @see ClientRunContext
 */
public final class ClientRunContexts {

  private ClientRunContexts() {
  }

  /**
   * Creates an empty {@link ClientRunContext} with <code>null</code> as preferred {@link Subject}, {@link Locale} and
   * {@link UserAgent}. Preferred means, that those values will not be derived from other values, e.g. when setting the
   * session, but must be set explicitly instead.
   */
  public static final ClientRunContext empty() {
    final ClientRunContext runContext = OBJ.get(ClientRunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }

  /**
   * Creates a "snapshot" of the current calling client context.
   */
  public static ClientRunContext copyCurrent() {
    final ClientRunContext runContext = OBJ.get(ClientRunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }
}
