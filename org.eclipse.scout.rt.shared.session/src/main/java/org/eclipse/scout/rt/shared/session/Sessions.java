/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.session;

import org.eclipse.scout.rt.shared.ISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to work with sessions.
 *
 * @since 5.2
 */
public final class Sessions {

  private static final Logger LOG = LoggerFactory.getLogger(Sessions.class);

  private Sessions() {
  }

  /**
   * @return session associated with the current thread, or <code>null</code> if not set, or if not of the expected
   * type.
   */
  public static <SESSION extends ISession> SESSION currentSession(final Class<SESSION> type) {
    ISession session = ISession.CURRENT.get();
    if (session == null) {
      return null;
    }
    if (!type.isInstance(session)) {
      LOG.debug("Session not of the expected type [session={}, expectedType={}]", session, type);
      return null;
    }
    return type.cast(session);
  }
}
