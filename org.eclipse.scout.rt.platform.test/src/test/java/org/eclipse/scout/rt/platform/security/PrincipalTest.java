/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import org.eclipse.scout.rt.testing.platform.security.AbstractPrincipalTest;
import org.ietf.jgss.GSSCredential;
import org.junit.Test;

public class PrincipalTest extends AbstractPrincipalTest {
  @Test
  public void testSimplePrinciple() {
    SimplePrincipal principal0 = new SimplePrincipal("Principle");
    SimplePrincipal principal1 = new SimplePrincipal("Principle1");
    SimplePrincipal principal2 = new SimplePrincipal("Principle");

    expectPrincipalToNotBeEqual(principal0, principal1);
    expectPrincipalToBeEqual(principal0, principal2);
  }

  @Test
  public void testJwtPrincipal() {
    JwtPrincipal p1 = new JwtPrincipal("user1", "jwt");
    JwtPrincipal p2 = new JwtPrincipal("user1", "jwt");
    JwtPrincipal p3 = new JwtPrincipal("user1", "jwt2");

    expectPrincipalToBeEqual(p1, p2);
    expectPrincipalToNotBeEqual(p1, p3);

    p1.setAccessToken("access");
    p1.setRefreshToken("refresh");
    p1.setOid("oid");

    p2.setAccessToken("access");
    p2.setRefreshToken("refresh");
    p2.setOid("oid");

    expectPrincipalToBeEqual(p1, p2);

    p2.setAccessToken("access1");
    p2.setRefreshToken("refresh");
    p2.setOid("oid");
    expectPrincipalToNotBeEqual(p1, p2);

    p2.setAccessToken("access");
    p2.setRefreshToken("refresh1");
    p2.setOid("oid");
    expectPrincipalToNotBeEqual(p1, p2);

    p2.setAccessToken("access");
    p2.setRefreshToken("refresh");
    p2.setOid("oid1");
    expectPrincipalToNotBeEqual(p1, p2);
  }

  @Test
  public void testSamlPrincipal() {
    SamlPrincipal principal0 = new SamlPrincipal("Principle", "sessionIndex");
    SamlPrincipal principal1 = new SamlPrincipal("Principle1", "sessionIndex");
    SamlPrincipal principal2 = new SamlPrincipal("Principle", "sessionIndex1");
    SamlPrincipal principal3 = new SamlPrincipal("Principle1", "sessionIndex1");
    SamlPrincipal principal4 = new SamlPrincipal("Principle", "sessionIndex");

    expectPrincipalToNotBeEqual(principal0, principal1);
    expectPrincipalToNotBeEqual(principal0, principal2);
    expectPrincipalToNotBeEqual(principal0, principal3);
    expectPrincipalToBeEqual(principal0, principal4);
  }

  @Test
  public void testSimplePrincipalWithDelegation() {
    GSSCredential clientCreds = new TestGSSCredential();
    GSSCredential clientCreds1 = new TestGSSCredential();

    SimplePrincipalWithDelegation principal0 = new SimplePrincipalWithDelegation("Principle", clientCreds);
    SimplePrincipalWithDelegation principal1 = new SimplePrincipalWithDelegation("Principle1", clientCreds);
    SimplePrincipalWithDelegation principal2 = new SimplePrincipalWithDelegation("Principle", clientCreds1);
    SimplePrincipalWithDelegation principal3 = new SimplePrincipalWithDelegation("Principle1", clientCreds1);
    SimplePrincipalWithDelegation principal4 = new SimplePrincipalWithDelegation("Principle", null);
    SimplePrincipalWithDelegation principal5 = new SimplePrincipalWithDelegation("Principle", clientCreds);

    expectPrincipalToNotBeEqual(principal0, principal1);
    expectPrincipalToNotBeEqual(principal0, principal2);
    expectPrincipalToNotBeEqual(principal0, principal3);
    expectPrincipalToNotBeEqual(principal0, principal4);
    expectPrincipalToBeEqual(principal0, principal5);
  }
}
