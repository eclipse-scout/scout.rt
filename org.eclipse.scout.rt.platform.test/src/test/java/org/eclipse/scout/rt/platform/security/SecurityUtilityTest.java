/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.security.ISecurityProvider.KeyPairBytes;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
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

    byte[] encryptData3 = SecurityUtility.encrypt(new byte[]{}, PASSWORD, salt, KEY_LEN);
    byte[] decryptedEmpty = SecurityUtility.decrypt(encryptData3, PASSWORD, salt, KEY_LEN);
    Assert.assertTrue(Arrays.equals(decryptedEmpty, new byte[]{}));

    String decryptedString = new String(SecurityUtility.decrypt(encryptData, PASSWORD, salt, KEY_LEN), ENCODING);
    Assert.assertEquals(origData, decryptedString);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncryptNoData() throws Exception {
    final byte[] salt = SecurityUtility.createRandomBytes();
    SecurityUtility.encrypt(null, "pass", salt, KEY_LEN);
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

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSignatureWrongArg1() throws Exception {
    SecurityUtility.createSignature(null, new byte[]{});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSignatureWrongArg2() throws Exception {
    SecurityUtility.createSignature(new byte[]{}, (byte[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSignatureWrongArg3() throws Exception {
    SecurityUtility.createSignature(new byte[]{}, (InputStream) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVerifySignatureWrongArg1() throws Exception {
    SecurityUtility.verifySignature(new byte[]{}, (byte[]) null, new byte[]{});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVerifySignatureWrongArg2() throws Exception {
    SecurityUtility.verifySignature(null, new byte[]{}, new byte[]{});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVerifySignatureWrongArg3() throws Exception {
    SecurityUtility.verifySignature(new byte[]{}, new byte[]{}, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVerifySignatureWrongArg4() throws Exception {
    SecurityUtility.verifySignature(new byte[]{}, (InputStream) null, new byte[]{});
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
    byte[] hash = SecurityUtility.hash(data, salt);
    byte[] hash2 = SecurityUtility.hash(data, salt);
    byte[] hash3 = SecurityUtility.hash(data, salt2);
    byte[] hash4 = SecurityUtility.hash(data, salt);
    byte[] hash5 = SecurityUtility.hash(new byte[]{}, salt);

    // ensure null input throws exception
    boolean nullNotAllowed = false;
    try {
      SecurityUtility.hash((byte[]) null, salt);
    }
    catch (IllegalArgumentException e) {
      nullNotAllowed = true;
    }
    Assert.assertTrue(nullNotAllowed);

    nullNotAllowed = false;
    try {
      SecurityUtility.hash((InputStream) null, salt);
    }
    catch (IllegalArgumentException e) {
      nullNotAllowed = true;
    }
    Assert.assertTrue(nullNotAllowed);

    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(data, hash));
    Assert.assertFalse(Arrays.equals(data, hash3));

    // ensure different salts matter
    Assert.assertFalse(Arrays.equals(hash, hash3));

    // ensure same input -> same output
    Assert.assertTrue(Arrays.equals(hash, hash2));

    // ensure different input -> different output
    Assert.assertFalse(Arrays.equals(hash4, hash5));
  }

  @Test
  public void testHashPassword() {
    if (!IS_JAVA_18_OR_NEWER) {
      // only supported in java 1.8 or newer
      return;
    }

    final String pwd = "testpwd";
    final byte[] salt = SecurityUtility.createRandomBytes();
    final byte[] salt2 = SecurityUtility.createRandomBytes();
    final int iterations = 10000;

    // test hash
    byte[] hash1 = SecurityUtility.hashPassword(pwd, salt, iterations);
    byte[] hash2 = SecurityUtility.hashPassword(pwd, salt2, iterations);
    byte[] hash3 = SecurityUtility.hashPassword(pwd, salt, iterations);
    byte[] hash4 = SecurityUtility.hashPassword("", salt, iterations);
    byte[] hash5 = SecurityUtility.hashPassword(pwd, salt, iterations + 1);

    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(pwd.getBytes(ENCODING), hash1));

    // ensure different salts matter
    Assert.assertFalse(Arrays.equals(hash1, hash2));

    // ensure same input -> same output
    Assert.assertTrue(Arrays.equals(hash1, hash3));

    // ensure different input -> different output
    Assert.assertFalse(Arrays.equals(hash4, hash1));

    // ensure different iterations matter
    Assert.assertFalse(Arrays.equals(hash5, hash1));

    // test invalid values
    boolean ok = false;
    try {
      SecurityUtility.hashPassword(null, salt, iterations);
    }
    catch (IllegalArgumentException e) {
      ok = true;
    }
    Assert.assertTrue(ok);

    ok = false;
    try {
      SecurityUtility.hashPassword(pwd, null, iterations);
    }
    catch (IllegalArgumentException e) {
      ok = true;
    }
    Assert.assertTrue(ok);

    ok = false;
    try {
      SecurityUtility.hashPassword(pwd, new byte[]{}, iterations);
    }
    catch (IllegalArgumentException e) {
      ok = true;
    }
    Assert.assertTrue(ok);
    ok = false;
    try {
      SecurityUtility.hashPassword(pwd, salt, 0);
    }
    catch (IllegalArgumentException e) {
      ok = true;
    }
    Assert.assertTrue(ok);
    ok = false;
    try {
      SecurityUtility.hashPassword(pwd, salt, -1);
    }
    catch (IllegalArgumentException e) {
      ok = true;
    }
    Assert.assertTrue(ok);
    ok = false;
    try {
      SecurityUtility.hashPassword(pwd, salt, 9999);
    }
    catch (IllegalArgumentException e) {
      ok = true;
    }
    Assert.assertTrue(ok);
  }

  @Test
  public void testCreateMac() {
    byte[] data = "testdata".getBytes();
    byte[] data2 = "testdata2".getBytes();
    byte[] mac1 = SecurityUtility.createMac("testpw".getBytes(), data);
    byte[] mac2 = SecurityUtility.createMac("testpwdiff".getBytes(), data);
    byte[] mac3 = SecurityUtility.createMac("testpwd".getBytes(), data2);

    Assert.assertFalse(Arrays.equals(mac1, mac2));
    Assert.assertFalse(Arrays.equals(mac3, mac2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateMacWrongArg1() throws Exception {
    SecurityUtility.createMac(null, new byte[]{});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateMacWrongArg2() throws Exception {
    SecurityUtility.createMac(new byte[]{}, (byte[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateMacWrongArg3() throws Exception {
    SecurityUtility.createMac(new byte[]{}, (InputStream) null);
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
