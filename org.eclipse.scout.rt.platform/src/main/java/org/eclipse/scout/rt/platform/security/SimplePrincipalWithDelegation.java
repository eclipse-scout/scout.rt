/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
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

import org.ietf.jgss.GSSCredential;

public class SimplePrincipalWithDelegation implements Principal {

  private final String m_name;
  private final GSSCredential m_delegatedCred;

  public SimplePrincipalWithDelegation(String name, final GSSCredential delegCred) {
    if (name == null) {
      throw new IllegalArgumentException("name must not be null");
    }
    m_name = name;
    m_delegatedCred = delegCred;
  }

  @Override
  public String getName() {
    return m_name;
  }

  public GSSCredential getDelegatedCred() {
    return m_delegatedCred;
  }
}
