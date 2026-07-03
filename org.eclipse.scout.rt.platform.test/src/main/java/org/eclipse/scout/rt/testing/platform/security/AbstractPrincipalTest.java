/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.security;

import static org.junit.Assert.*;

import java.security.Principal;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

public abstract class AbstractPrincipalTest {
  protected void expectPrincipalToNotBeEqual(Principal principal1, Principal principal2) {
    assertNotEquals(principal1, principal2);
  }

  protected void expectPrincipalToBeEqual(Principal principal1, Principal principal2) {
    assertEquals(principal1, principal2);
    assertEquals(principal1.hashCode(), principal2.hashCode());
  }

  public static class TestGSSCredential implements GSSCredential {

    @Override
    public void dispose() {
    }

    @Override
    public GSSName getName() {
      return null;
    }

    @Override
    public GSSName getName(Oid mech) {
      return null;
    }

    @Override
    public int getRemainingLifetime() {
      return INDEFINITE_LIFETIME;
    }

    @Override
    public int getRemainingInitLifetime(Oid mech) {
      return 0;
    }

    @Override
    public int getRemainingAcceptLifetime(Oid mech) {
      return 0;
    }

    @Override
    public int getUsage() {
      return 0;
    }

    @Override
    public int getUsage(Oid mech) {
      return 0;
    }

    @Override
    public Oid[] getMechs() {
      return new Oid[0];
    }

    @Override
    public void add(GSSName name, int initLifetime, int acceptLifetime, Oid mech, int usage) {
    }
  }
}
