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
