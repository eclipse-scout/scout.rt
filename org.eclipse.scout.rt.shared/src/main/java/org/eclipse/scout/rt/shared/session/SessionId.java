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

import java.math.BigInteger;

import org.eclipse.scout.rt.platform.security.SecurityUtility;

public final class SessionId {

  private SessionId() {
  }

  /**
   * The {@code session id} of the client session which is currently associated with the current thread.
   */
  public static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

  /**
   * Name of the HTTP header to transport the {@code session id}.
   */
  public static final String HTTP_HEADER_NAME = "X-Scout-Client-Session-Id";

  /**
   * @return random session ID to be used when creating a session
   */
  public static String randomSessionId() {
    // see https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/Session_Management_Cheat_Sheet.md
    BigInteger randomId = new BigInteger(1, SecurityUtility.createRandomBytes());

    // use Base32 encoding because it is shorter than hex and does not include special characters and is case-insensitive (compared to Base64).
    return randomId.toString(32);
  }
}
