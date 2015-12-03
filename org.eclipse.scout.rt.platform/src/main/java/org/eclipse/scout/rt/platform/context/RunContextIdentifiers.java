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

import java.util.Deque;

/**
 * Run Context Identifiers thread local.<br>
 *
 * @since 5.2
 */
public final class RunContextIdentifiers {

  /**
   * Holds the currently active run context identifiers.
   */
  public static final ThreadLocal<Deque<String>> CURRENT = new ThreadLocal<>();

  /**
   * Checks if the given identifier is on top of the identifiers for the active context.
   *
   * @param identifier
   *          The identifier to check.
   * @return <code>true</code> if the given identifier is the currently active identifier (which means it is on top of
   *         the {@link Deque}). Returns <code>false</code> otherwise.
   */
  public static boolean isCurrent(String identifier) {
    Deque<String> idsOfCurrentContext = CURRENT.get();
    return idsOfCurrentContext != null && identifier != null && identifier.equals(idsOfCurrentContext.peek());
  }
}
