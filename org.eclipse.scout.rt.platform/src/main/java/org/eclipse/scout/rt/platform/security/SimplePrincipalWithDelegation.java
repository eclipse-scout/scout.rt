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
