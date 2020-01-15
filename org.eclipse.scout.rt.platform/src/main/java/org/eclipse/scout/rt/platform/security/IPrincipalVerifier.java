/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
   *          the principal to verify
   * @return Result of the verification; true if valid, false if invalid
   */
  boolean verify(Principal principal);
}
