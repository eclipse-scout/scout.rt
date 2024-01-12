/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.junit.Assert;
import org.junit.Test;

public class LegacySecurityProviderTest {

  private static final Charset ENCODING = StandardCharsets.UTF_8;

  @Test
  public void testCreateHash() {
    ILegacySecurityProvider securityProvider = BEANS.get(ILegacySecurityProvider.class);
    final byte[] data = "testdata".getBytes(ENCODING);
    final byte[] salt = SecurityUtility.createRandomBytes();
    final byte[] salt2 = SecurityUtility.createRandomBytes();
    byte[] hash = securityProvider.createHash(data, salt);
    byte[] hash2 = securityProvider.createHash(data, salt);
    byte[] hash3 = securityProvider.createHash(data, salt2);
    byte[] hash4 = securityProvider.createHash(data, salt);
    byte[] hash5 = securityProvider.createHash(new byte[]{}, salt);

    // ensure null input throws exception
    boolean nullNotAllowed = false;
    try {
      securityProvider.createHash(null, salt);
    }
    catch (AssertionException e) {
      nullNotAllowed = true;
    }
    Assert.assertTrue(nullNotAllowed);

    nullNotAllowed = false;
    try {
      securityProvider.createHash(null, salt, 1234);
    }
    catch (AssertionException e) {
      nullNotAllowed = true;
    }
    Assert.assertTrue(nullNotAllowed);

    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(data, hash));
    Assert.assertFalse(Arrays.equals(data, hash3));

    // ensure different salts matter
    Assert.assertFalse(Arrays.equals(hash, hash3));

    // ensure same input -> same output
    Assert.assertArrayEquals(hash, hash2);

    // ensure different input -> different output
    Assert.assertFalse(Arrays.equals(hash4, hash5));

    // ensure unchanged hashing result compared to legacy security provider
    Assert.assertArrayEquals(SecurityUtility.hash(data), securityProvider.createHash(new ByteArrayInputStream(data), null, 1));
  }

  @Test
  public void testCreateHashStability() {
    final byte[] data = "my text to hash".getBytes(ENCODING);
    final byte[] salt = "mycustomsalt".getBytes(ENCODING);
    Assert.assertEquals("wFxcmwxtdbR1is2XNDHPUfFA9Q60Cnt5geHAfs+LIxn4hxjmSBLbyzNmp9g8MB5Is3NNAs2XzjRN0byuv5mnBg==", Base64Utility.encode(BEANS.get(ILegacySecurityProvider.class).createHash(data, salt)));
  }
}
