/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
