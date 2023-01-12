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

import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegationCredentialLifetimeVerifier implements IPrincipalVerifier {

  private static final Logger LOG = LoggerFactory.getLogger(DelegationCredentialLifetimeVerifier.class);

  @Override
  public boolean verify(Principal principal0) {
    if (!(principal0 instanceof SimplePrincipalWithDelegation)) {
      return true;
    }
    SimplePrincipalWithDelegation principal = (SimplePrincipalWithDelegation) principal0;
    if (principal.getDelegatedCred() == null) {
      return true;
    }
    try {
      return principal.getDelegatedCred().getRemainingLifetime() > 120;
    }
    catch (GSSException e) {
      LOG.error("Error reading delegation credentials lifetime", e);
      return false;
    }
  }
}
