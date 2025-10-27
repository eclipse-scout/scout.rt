/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.ConfigFileCredentialVerifier.CredentialsProperty;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Testcases for {@link ConfigFileCredentialVerifier}
 */
public class ConfigFileCredentialVerifierTest {

  @Rule
  public MockConfigPropertyRule<String> m_credentialPropertyRule = new MockConfigPropertyRule<>(CredentialsProperty.class, "john:K9LGUhLTiJol/HRmvtwkqUDOZnzY3H8V+eqLHU+JvY4=.oV6R5uNo/1qH+vQMcnreeF+3kOf/8QQ9iYaagmXTpEs=");

  @Test
  public void testVerify_OK() throws IOException {
    ICredentialVerifier verifier = BEANS.get(ConfigFileCredentialVerifier.class);
    assertEquals(ICredentialVerifier.AUTH_OK, verifier.verify("john", "mock-secret".toCharArray()));
    assertEquals(ICredentialVerifier.AUTH_OK, verifier.verify("John", "mock-secret".toCharArray()));
    assertEquals(ICredentialVerifier.AUTH_OK, verifier.verify("JOHN", "mock-secret".toCharArray()));
    assertEquals(ICredentialVerifier.AUTH_OK, verifier.verify("jOhN", "mock-secret".toCharArray()));
  }

  @Test
  public void testVerify_Forbidden() throws IOException {
    ICredentialVerifier verifier = BEANS.get(ConfigFileCredentialVerifier.class);
    assertEquals(ICredentialVerifier.AUTH_FORBIDDEN, verifier.verify("john", "mock-wrong-secret".toCharArray()));
  }

  @Test
  public void testVerify_CredentialsRequired() throws IOException {
    ICredentialVerifier verifier = BEANS.get(ConfigFileCredentialVerifier.class);
    assertEquals(ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED, verifier.verify("john", null));
    assertEquals(ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED, verifier.verify(null, "mock-secret".toCharArray()));
    assertEquals(ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED, verifier.verify("john", "".toCharArray()));
  }
}
