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

import java.math.BigInteger;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.security.IAccessControlService;
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

  /**
   * @return random session ID to be used when creating a session
   */
  public static String randomSessionId() {
    // see https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/Session_Management_Cheat_Sheet.md
    BigInteger randomId = new BigInteger(1, SecurityUtility.createRandomBytes());

    // use Base32 encoding because it is shorter than hex and does not include special characters and is case-insensitive (compared to Base64).
    return randomId.toString(32);
  }

  /**
   * First, tries to get user id from session associated with current thread. If no active session can be found,
   * {@link IAccessControlService#getUserIdOfCurrentSubject()} is called.
   *
   * @return current user id or <code>null</code> if user id can not be extracted from current {@link RunContext} and
   * {@link Subject}
   */
  public static String getCurrentUserId() {
    ISession session = ISession.CURRENT.get();
    if (session != null && session.isActive()) {
      // only an active session has a valid userId
      return session.getUserId();
    }
    return BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
  }
}
