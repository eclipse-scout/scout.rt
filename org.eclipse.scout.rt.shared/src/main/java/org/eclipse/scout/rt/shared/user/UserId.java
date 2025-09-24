/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.user;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.security.IAccessControlService;

public final class UserId {

  private UserId() {
  }

  /**
   * The {@code userId} which is currently associated with the current thread. The {@code userId} is extracted from current
   * {@link Subject} using {@link IAccessControlService#getUserIdOfCurrentSubject()} and usually corresponds to the username the of logged-in user.
   */
  public static final ThreadLocal<String> CURRENT = new ThreadLocal<>();
}
