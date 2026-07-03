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
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SimplePrincipalWithDelegation that)) {
      return false;
    }
    // See {@link sun.security.jgss.GSSCredentialImpl#equals}: Could potentially throw en exception when already disposed otherwise directly compares objects anyways
    return Objects.equals(m_name, that.m_name) && m_delegatedCred == that.m_delegatedCred;
  }

  @Override
  public int hashCode() {
    // See {@link sun.security.jgss.GSSCredentialImpl#hashCode}: Could potentially throw en exception when already disposed and would return 1 anyways
    return Objects.hash(m_name, m_delegatedCred != null ? 1 : 0);
  }
}
