/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.session;

import java.util.EventListener;

import org.eclipse.scout.rt.shared.ISession;

/**
 * Listener to be notified about a specific session state changes.
 * <p>
 * The listener must be manually registered by calling {@link ISession#addListener(ISessionListener)}.
 * <p>
 * If a global session listener is required, use {@link IGlobalSessionListener} instead.
 *
 * @since 5.1
 */
@FunctionalInterface
public interface ISessionListener extends EventListener {

  /**
   * Method invoked once the session state changed.
   */
  void sessionChanged(SessionEvent event);

}
