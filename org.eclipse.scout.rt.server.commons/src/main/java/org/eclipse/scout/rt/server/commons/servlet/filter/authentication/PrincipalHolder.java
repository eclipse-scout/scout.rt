/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.security.Principal;

import org.eclipse.scout.rt.platform.holders.Holder;

/**
 * @deprecated will be removed in release 6.0; is to be replaced with a project specific ServletFilter with the
 *             authenticators chained yourself; see depreciation note of {@link AbstractChainableSecurityFilter}
 */
@Deprecated
public class PrincipalHolder extends Holder<Principal> {
  private static final long serialVersionUID = 1L;

  public PrincipalHolder() {
    super(Principal.class);
  }

  public void setPrincipal(Principal principal) {
    setValue(principal);
  }

  public Principal getPrincipal() {
    return getValue();
  }
}
