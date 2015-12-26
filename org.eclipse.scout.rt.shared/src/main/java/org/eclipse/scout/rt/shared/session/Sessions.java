package org.eclipse.scout.rt.shared.session;

import java.util.UUID;

import org.eclipse.scout.rt.platform.util.TypeCastUtility;
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
   * Returns the session associated with the current thread, or <code>null</code> if not set, or if not of the expected
   * type.
   */
  public static final <SESSION extends ISession> SESSION currentSession(final Class<SESSION> type) {
    final ISession session = ISession.CURRENT.get();
    if (session == null) {
      return null;
    }

    try {
      return TypeCastUtility.castValue(session, type);
    }
    catch (final IllegalArgumentException e) {
      LOG.debug("Session not of the expected type [session={}, expectedType={}]", session, type);
      return null;
    }
  }

  /**
   * Returns a random session ID to be used when creating a session.
   */
  public static String randomSessionId() {
    return UUID.randomUUID().toString();
  }
}
