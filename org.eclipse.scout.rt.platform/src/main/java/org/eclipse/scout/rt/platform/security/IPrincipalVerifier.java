/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.security.Principal;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
@FunctionalInterface
public interface IPrincipalVerifier {
  /**
   * Attempts to verify the given principal.
   *
   * @param principal
   *     the principal to verify
   * @return Result of the verification; true if valid, false if invalid
   */
  boolean verify(Principal principal);
}
