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
}
