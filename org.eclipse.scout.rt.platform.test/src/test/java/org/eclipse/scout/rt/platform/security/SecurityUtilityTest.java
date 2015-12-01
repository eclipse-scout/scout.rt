/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.security.SecurityUtility.KeyPairBytes;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.junit.Assert;
import org.junit.Test;

public class SecurityUtilityTest {

  private static final boolean IS_JAVA_18_OR_NEWER = CompareUtility.compareTo(System.getProperty("java.version"), "1.8") >= 0;
  private static final int KEY_LEN = 128;
  private static final Charset ENCODING = StandardCharsets.UTF_8;
  private static final String PASSWORD = "insecure";

  @Test
  public void testEncryption() throws Exception {
    if (!IS_JAVA_18_OR_NEWER) {
      // encryption only supported in java 1.8 or newer
      return;
    }

    final String origData = "origData";
    final byte[] salt = SecurityUtility.createRandomBytes();
    final byte[] inputBytes = origData.getBytes(ENCODING);

    byte[] encryptData = SecurityUtility.encrypt(inputBytes, PASSWORD, salt, KEY_LEN);
    Assert.assertFalse(Arrays.equals(encryptData, inputBytes));

    byte[] encryptData2 = SecurityUtility.encrypt(null, PASSWORD, salt, KEY_LEN);
    Assert.assertNull(encryptData2);

    byte[] encryptData3 = SecurityUtility.encrypt(new byte[]{}, PASSWORD, salt, KEY_LEN);
    Assert.assertTrue(Arrays.equals(encryptData3, new byte[]{}));

    String decryptedString = new String(SecurityUtility.decrypt(encryptData, PASSWORD, salt, KEY_LEN), ENCODING);
    Assert.assertEquals(origData, decryptedString);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncryptNoSalt() throws Exception {
    SecurityUtility.encrypt("test".getBytes(ENCODING), "pass", null, KEY_LEN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncryptNoKey() throws Exception {
    final byte[] salt = SecurityUtility.createRandomBytes();
    SecurityUtility.encrypt("test".getBytes(ENCODING), null, salt, KEY_LEN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncryptWrongKeyLen() throws Exception {
    final byte[] salt = SecurityUtility.createRandomBytes();
    SecurityUtility.encrypt("test".getBytes(ENCODING), "pass", salt, 4);
  }

  @Test
  public void testRandomBytes() throws Exception {
    byte[] createRandomBytes = SecurityUtility.createRandomBytes();
    byte[] createRandomBytes2 = SecurityUtility.createRandomBytes();
    SecurityUtility.createRandomBytes(4);
    Assert.assertFalse(Arrays.equals(createRandomBytes, createRandomBytes2));

    // ensure wrong input generates exception
    boolean illegalExc = false;
    try {
      SecurityUtility.createRandomBytes(0);
    }
    catch (IllegalArgumentException e) {
      illegalExc = true;
    }
    Assert.assertTrue(illegalExc);
  }

  @Test
  public void testHash() throws Exception {
    final byte[] data = "testdata".getBytes(ENCODING);
    final byte[] salt = SecurityUtility.createRandomBytes();
    final byte[] salt2 = SecurityUtility.createRandomBytes();
    byte[] hash = SecurityUtility.hash(data, salt, 1);
    byte[] hash2 = SecurityUtility.hash(data, salt);
    byte[] hash3 = SecurityUtility.hash(data, salt2, 1);
    byte[] hash4 = SecurityUtility.hash(data, null, 1);
    byte[] hash5 = SecurityUtility.hash(data, null, 1);
    byte[] hash6 = SecurityUtility.hash(data, null);
    byte[] hash7 = SecurityUtility.hash(data, salt);
    byte[] hash8 = SecurityUtility.hash(new byte[]{}, salt);

    // ensure null input throws exception
    boolean nullNotAllowed = false;
    try {
      SecurityUtility.hash(null, salt);
    }
    catch (IllegalArgumentException e) {
      nullNotAllowed = true;
    }
    Assert.assertTrue(nullNotAllowed);

    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(data, hash));
    Assert.assertFalse(Arrays.equals(data, hash2));
    Assert.assertFalse(Arrays.equals(data, hash3));

    // ensure different iterations matter
    Assert.assertFalse(Arrays.equals(hash, hash2));
    Assert.assertFalse(Arrays.equals(hash5, hash6));

    // ensure different salts matter
    Assert.assertFalse(Arrays.equals(hash, hash3));

    // ensure same input -> same output
    Assert.assertArrayEquals(hash2, hash7);
    Assert.assertArrayEquals(hash4, hash5);

    // ensure different input -> different output
    Assert.assertFalse(Arrays.equals(hash7, hash8));
  }

  @Test
  public void testGenerateKeyPair() throws Exception {
    KeyPairBytes generateKeyPair1 = SecurityUtility.generateKeyPair();
    KeyPairBytes generateKeyPair2 = SecurityUtility.generateKeyPair();

    Assert.assertFalse(Arrays.equals(generateKeyPair1.getPrivateKey(), generateKeyPair2.getPrivateKey()));
    Assert.assertFalse(Arrays.equals(generateKeyPair1.getPublicKey(), generateKeyPair2.getPublicKey()));
    Assert.assertFalse(Arrays.equals(generateKeyPair1.getPublicKey(), generateKeyPair1.getPrivateKey()));
    Assert.assertFalse(Arrays.equals(generateKeyPair2.getPublicKey(), generateKeyPair2.getPrivateKey()));

    Assert.assertTrue(generateKeyPair1.getPrivateKey().length > 0);
    Assert.assertTrue(generateKeyPair1.getPublicKey().length > 0);
    Assert.assertTrue(generateKeyPair2.getPrivateKey().length > 0);
    Assert.assertTrue(generateKeyPair2.getPublicKey().length > 0);
  }

  @Test
  public void testSignature() throws Exception {
    KeyPairBytes keyPair = SecurityUtility.generateKeyPair();
    final byte[] data = "original test data".getBytes(ENCODING);

    byte[] signature = SecurityUtility.createSignature(keyPair.getPrivateKey(), data);
    Assert.assertTrue(signature.length > 0);
    boolean valid = SecurityUtility.verifySignature(keyPair.getPublicKey(), data, signature);
    Assert.assertTrue(valid);
  }

  @Test
  public void testSignatureApiStability() throws Exception {
    final byte[] data = "myTestData".getBytes(ENCODING);
    final byte[] sig = Base64Utility.decode("MEUCIE+t3/ngQ65qql7bTFCPPGVbj2z0BIiwNzjaC6wMV93VAiEAxBTA6FWTCBVDSAMvk7FXZcUeF/i4lNc/fW4a2G63O64=");
    final byte[] pubKey = Base64Utility.decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEJX6uq87Qz1Xyk85+8NG5dnYDV1ZBSt/W3/H4nvY7h4o7JNM0AYxgX5/4Dizb5iD9iVX2uuv1BO7J9H/hze7Cag==");
    Assert.assertTrue(SecurityUtility.verifySignature(pubKey, data, sig));
  }

  @Test
  public void testDecryptionApiStability() throws Exception {
    if (!IS_JAVA_18_OR_NEWER) {
      // encryption only supported in java 1.8 or newer
      return;
    }

    final byte[] encrypted = Base64Utility.decode("43aysPcKhTvyzZIWa6d1wwntGobQOXT38VU=");
    final byte[] salt = Base64Utility.decode("iPENJpMTU8MxarL8ZMHxXw==");
    Assert.assertEquals("myTestData", new String(SecurityUtility.decrypt(encrypted, PASSWORD, salt, 128), ENCODING));
  }
}
