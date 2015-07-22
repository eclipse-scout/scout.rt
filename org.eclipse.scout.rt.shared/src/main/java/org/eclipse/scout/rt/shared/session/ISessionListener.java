/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
public interface ISessionListener extends EventListener {

  /**
   * Method invoked once the session state changed.
   */
  void sessionChanged(SessionEvent event);

}
