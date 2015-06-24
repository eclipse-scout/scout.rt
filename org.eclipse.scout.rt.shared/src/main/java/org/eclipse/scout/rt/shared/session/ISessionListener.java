/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.session;

import java.util.EventListener;

/**
 * Listener to be notified about session state changes.
 *
 * @since 5.1
 */
public interface ISessionListener extends EventListener {

  /**
   * Method invoked once the session state changed.
   */
  void sessionChanged(SessionEvent event);

}
