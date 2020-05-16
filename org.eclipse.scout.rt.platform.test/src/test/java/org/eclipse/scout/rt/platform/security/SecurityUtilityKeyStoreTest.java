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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtilityKeyStoreTest {
  private static final Logger LOG = LoggerFactory.getLogger(SecurityUtilityKeyStoreTest.class);

  private File m_file;

  @Before
  public void before() throws IOException {
    m_file = File.createTempFile("KeyStoreUtilityTest", ".txt");
    m_file.deleteOnExit();
  }

  @After
  public void after() {
    if (m_file != null && m_file.exists()) {
      assertTrue(m_file.delete());
    }
  }

  @Test
  public void testCreateSelfSignedCertificate() throws IOException {
    try (FileOutputStream out = new FileOutputStream(m_file)) {
      SecurityUtility.createSelfSignedCertificate("test-alias", "CN=myhost.mydomain.com,C=US,S=CA,L=Sunnyvale,O=My Company Inc.", "store-x", "key-x", 4096, 365, out);
    }

    String textWithPrivateKey = SecurityUtility.keyStoreToHumanReadableText(m_file.toURI().toString(), "store-x", "key-x");
    LOG.info("textWithPrivateKey: \n{}", textWithPrivateKey);
    assertTrue(textWithPrivateKey.contains("Alias: test-alias"));
    assertTrue(textWithPrivateKey.contains("subjectDN: CN=myhost.mydomain.com, C=US, ST=CA, L=Sunnyvale, O=My Company Inc."));
    assertTrue(textWithPrivateKey.contains("PrivateKey"));

    String textWithoutPrivateKey = SecurityUtility.keyStoreToHumanReadableText(m_file.toURI().toString(), "store-x", null);
    LOG.info("textWithoutPrivateKey: \n{}", textWithoutPrivateKey);
    assertTrue(textWithoutPrivateKey.contains("Alias: test-alias"));
    assertTrue(textWithoutPrivateKey.contains("subjectDN: CN=myhost.mydomain.com, C=US, ST=CA, L=Sunnyvale, O=My Company Inc."));
    assertFalse(textWithoutPrivateKey.contains("PrivateKey"));
  }
}
