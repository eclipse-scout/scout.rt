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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class SecurityUtilityTest {

  private static final int KEY_LEN = 128;
  private static final Charset ENCODING = StandardCharsets.UTF_8;
  private static final char[] PASSWORD = "insecure".toCharArray();

  @Test
  public void testEncryption() {
    final String origData = "origData";
    final byte[] salt = SecurityUtility.createRandomBytes();
    final byte[] inputBytes = origData.getBytes(ENCODING);

    byte[] encryptData = SecurityUtility.encrypt(inputBytes, PASSWORD, salt, KEY_LEN);
    Assert.assertFalse(Arrays.equals(encryptData, inputBytes));

    byte[] encryptData3 = SecurityUtility.encrypt(new byte[]{}, PASSWORD, salt, KEY_LEN);
    byte[] decryptedEmpty = SecurityUtility.decrypt(encryptData3, PASSWORD, salt, KEY_LEN);
    Assert.assertArrayEquals(decryptedEmpty, new byte[]{});

    String decryptedString = new String(SecurityUtility.decrypt(encryptData, PASSWORD, salt, KEY_LEN), ENCODING);
    Assert.assertEquals(origData, decryptedString);
  }

  @Test(expected = AssertionException.class)
  public void testEncryptNoData() {
    final byte[] salt = SecurityUtility.createRandomBytes();
    SecurityUtility.encrypt(null, "pass".toCharArray(), salt, KEY_LEN);
  }

  @Test(expected = AssertionException.class)
  public void testEncryptNoSalt() {
    SecurityUtility.encrypt("test".getBytes(ENCODING), "pass".toCharArray(), null, KEY_LEN);
  }

  @Test(expected = AssertionException.class)
  public void testEncryptNoKey() {
    final byte[] salt = SecurityUtility.createRandomBytes();
    SecurityUtility.encrypt("test".getBytes(ENCODING), null, salt, KEY_LEN);
  }

  @Test(expected = AssertionException.class)
  public void testEncryptWrongKeyLen() {
    final byte[] salt = SecurityUtility.createRandomBytes();
    SecurityUtility.encrypt("test".getBytes(ENCODING), "pass".toCharArray(), salt, 4);
  }

  @Test(expected = AssertionException.class)
  public void testCreateSignatureWrongArg1() {
    SecurityUtility.createSignature(null, new byte[]{});
  }

  @Test(expected = AssertionException.class)
  public void testCreateSignatureWrongArg2() {
    SecurityUtility.createSignature(new byte[]{}, (byte[]) null);
  }

  @Test(expected = AssertionException.class)
  public void testCreateSignatureWrongArg3() {
    SecurityUtility.createSignature(new byte[]{}, (InputStream) null);
  }

  @Test(expected = AssertionException.class)
  public void testVerifySignatureWrongArg1() {
    SecurityUtility.verifySignature(new byte[]{}, (byte[]) null, new byte[]{});
  }

  @Test(expected = AssertionException.class)
  public void testVerifySignatureWrongArg2() {
    SecurityUtility.verifySignature(null, new byte[]{}, new byte[]{});
  }

  @Test(expected = AssertionException.class)
  public void testVerifySignatureWrongArg3() {
    SecurityUtility.verifySignature(new byte[]{}, new byte[]{}, null);
  }

  @Test(expected = AssertionException.class)
  public void testVerifySignatureWrongArg4() {
    SecurityUtility.verifySignature(new byte[]{}, (InputStream) null, new byte[]{});
  }

  @Test
  public void testRandomBytes() {
    byte[] createRandomBytes = SecurityUtility.createRandomBytes();
    byte[] createRandomBytes2 = SecurityUtility.createRandomBytes();
    SecurityUtility.createRandomBytes(4);
    Assert.assertFalse(Arrays.equals(createRandomBytes, createRandomBytes2));

    // ensure wrong input generates exception
    boolean illegalExc = false;
    try {
      SecurityUtility.createRandomBytes(0);
    }
    catch (AssertionException e) {
      illegalExc = true;
    }
    Assert.assertTrue(illegalExc);
  }

  @Test
  public void testHash() {
    final byte[] data = "testdata".getBytes(ENCODING);
    byte[] hash = SecurityUtility.hash(data);
    byte[] hash2 = SecurityUtility.hash(data);
    byte[] hash3 = SecurityUtility.hash(new byte[]{});

    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(data, hash));

    // ensure same input -> same output
    Assert.assertArrayEquals(hash, hash2);

    // ensure different input -> different output
    Assert.assertFalse(Arrays.equals(hash, hash3));

    // ensure unchanged hashing result compared to legacy security provider
    Assert.assertArrayEquals(BEANS.get(ILegacySecurityProvider.class).createHash(new ByteArrayInputStream(data), null, 1), hash);
  }

  @Test
  public void testHashStability() {
    final byte[] data = "my text to hash".getBytes(ENCODING);
    Assert.assertEquals("JqpRPiojKrf7Pb4rkfTrLjyz5noCYCWLJVVQo3TwV+t83ZG6dR6QNyB5yXbBL1fhkO0Xh0qr05Xor33blAuHrw==", Base64Utility.encode(SecurityUtility.hash(data)));
  }

  @Test
  public void testToHashingStream_InputStream() throws IOException {
    final byte[] data = "test.input.stream".getBytes(ENCODING);
    DigestInputStream hashingStream = SecurityUtility.toHashingStream(new ByteArrayInputStream(data));
    byte[] hash = hashingStream.getMessageDigest().digest();
    // ensure data has been read
    Assert.assertArrayEquals(data, hashingStream.readAllBytes());
    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(hash, hashingStream.getMessageDigest().digest()));
  }

  @Test
  public void testToHashingStream_OutputStream() throws IOException {
    final byte[] data = "test.output.stream".getBytes(ENCODING);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DigestOutputStream hashingStream = SecurityUtility.toHashingStream(outputStream);
    byte[] hash = hashingStream.getMessageDigest().digest();
    // ensure data has been written
    hashingStream.write(data);
    Assert.assertArrayEquals(data, outputStream.toByteArray());
    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(hash, hashingStream.getMessageDigest().digest()));
  }

  @Test
  public void testHashLegacyPassword() {
    char[] password = "test.1234".toCharArray();
    byte[] salt = Base64Utility.decode("NSZei2H8Y5YYMzGXe+tiSbJ6TeKEN1sNR7ovARa4OZE=");
    byte[] expectedHash = Base64Utility.decode("qNGznjbmYm8p3Aihh3DLX5sZcHOYXJ2icH2t7zXHObNDqr4J2dzBv7J1//PkWqXLMpCs7kEGIBxq6ukslJOA2g==");

    ByteBuffer bytes = StandardCharsets.UTF_16.encode(CharBuffer.wrap(password));
    byte[] passwordBytes = new byte[bytes.remaining()];
    bytes.get(passwordBytes);
    Assert.assertArrayEquals(expectedHash, BEANS.get(ILegacySecurityProvider.class).createHash(passwordBytes, salt));
    Assert.assertTrue(SecurityUtility.verifyPasswordHash(password, salt, expectedHash));
  }

  @Test
  public void testHashPassword() {
    final byte[] salt = SecurityUtility.createRandomBytes();
    final byte[] salt2 = SecurityUtility.createRandomBytes();

    // test hash
    byte[] hash1 = SecurityUtility.hashPassword(PASSWORD, salt);
    byte[] hash2 = SecurityUtility.hashPassword(PASSWORD, salt2);
    byte[] hash3 = SecurityUtility.hashPassword(PASSWORD, salt);
    byte[] hash4 = SecurityUtility.hashPassword("other".toCharArray(), salt);

    // ensure hashing was executed
    Assert.assertFalse(Arrays.equals(String.valueOf(PASSWORD).getBytes(ENCODING), hash1));

    // ensure different salts matter
    Assert.assertFalse(Arrays.equals(hash1, hash2));

    // ensure same input -> same output
    Assert.assertArrayEquals(hash1, hash3);

    // ensure different input -> different output
    Assert.assertFalse(Arrays.equals(hash4, hash1));

    // test invalid values
    boolean ok = false;
    try {
      SecurityUtility.hashPassword(null, salt);
    }
    catch (AssertionException e) {
      ok = true;
    }
    Assert.assertTrue(ok);

    ok = false;
    try {
      SecurityUtility.hashPassword(PASSWORD, null);
    }
    catch (AssertionException e) {
      ok = true;
    }
    Assert.assertTrue(ok);
    ok = false;
    try {
      SecurityUtility.hashPassword("".toCharArray(), salt);
    }
    catch (AssertionException e) {
      ok = true;
    }
    Assert.assertTrue(ok);
    ok = false;
    try {
      SecurityUtility.hashPassword(PASSWORD, new byte[]{});
    }
    catch (AssertionException e) {
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

  @Test(expected = AssertionException.class)
  public void testCreateMacWrongArg1() {
    SecurityUtility.createMac(null, new byte[]{});
  }

  @Test(expected = AssertionException.class)
  public void testCreateMacWrongArg2() {
    SecurityUtility.createMac(new byte[]{}, (byte[]) null);
  }

  @Test(expected = AssertionException.class)
  public void testCreateMacWrongArg3() {
    SecurityUtility.createMac(new byte[]{}, (InputStream) null);
  }

  @Test
  public void testGenerateKeyPair() {
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
  public void testSignature() {
    KeyPairBytes keyPair = SecurityUtility.generateKeyPair();
    final byte[] data = "original test data".getBytes(ENCODING);

    byte[] signature = SecurityUtility.createSignature(keyPair.getPrivateKey(), data);
    Assert.assertTrue(signature.length > 0);
    boolean valid = SecurityUtility.verifySignature(keyPair.getPublicKey(), data, signature);
    Assert.assertTrue(valid);
  }

  @Test
  public void testSignatureApiStability() {
    final byte[] data = "myTestData".getBytes(ENCODING);
    final byte[] sig = Base64Utility.decode("MEUCIDZoyslIxALDkonxJKwMnk6v7uyu8T50cch+cU1EPnL/AiEAuGLvbW+CvUKTKYY7t5j75TTkjUfLevOXg7C53GoT/Sg=");
    final byte[] pubKey = Base64Utility.decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7KTCxVbBVVqNWELFCR33K8LUrfmp6psbH1AXwD/ezzhad7mMcTEd23ZUmxT4RZ75DYmGFRGImpqhpdwWNrbCPg==");
    Assert.assertTrue(SecurityUtility.verifySignature(pubKey, data, sig));
  }

  @Test
  public void testDecryptionApiStability() {
    final byte[] encrypted = Base64Utility.decode("43aysPcKhTvyzZIWa6d1wwntGobQOXT38VU=");
    final byte[] salt = Base64Utility.decode("iPENJpMTU8MxarL8ZMHxXw==");
    Assert.assertEquals("myTestData", new String(SecurityUtility.decrypt(encrypted, PASSWORD, salt, 128), ENCODING));
    EncryptionKey key = SecurityUtility.createDecryptionKey(new PushbackInputStream(new ByteArrayInputStream(encrypted), 6), PASSWORD, salt, 128, null);
    Assert.assertEquals("[1:128-PBKDF2WithHmacSHA256-AES-SunJCE-16-128-3557]", new String(key.getCompatibilityHeader(), StandardCharsets.US_ASCII));
    Assert.assertEquals("myTestData", new String(SecurityUtility.decrypt(encrypted, key), ENCODING));
  }

  @Test
  public void testExtractCompatibilityHeader() {
    PushbackInputStream in = new PushbackInputStream(new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5}), 6);
    Assert.assertNull(SecurityUtility.extractCompatibilityHeader(in));
    Assert.assertArrayEquals(new byte[]{0, 1, 2, 3, 4, 5}, IOUtility.readBytes(in));

    in = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'[', 1, 2, 3, -1, ':'}), 6);
    Assert.assertNull(SecurityUtility.extractCompatibilityHeader(in));
    Assert.assertArrayEquals(new byte[]{'[', 1, 2, 3, -1, ':'}, IOUtility.readBytes(in));

    in = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'[', '1', '2', '3', '0', ':'}), 6);
    Assert.assertArrayEquals(new byte[]{'[', 49, 50, 51, 48, ':'}, SecurityUtility.extractCompatibilityHeader(in));
    Assert.assertArrayEquals(new byte[]{}, IOUtility.readBytes(in));

    in = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'[', '1', '2', '3', '0', 77, 78, 79}), 6);
    Assert.assertNull(SecurityUtility.extractCompatibilityHeader(in));
    Assert.assertArrayEquals(new byte[]{'[', '1', '2', '3', '0', 77, 78, 79}, IOUtility.readBytes(in));

    in = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'[', '1', '2', '3', '0', ':', 0, ']', 78, 79}), 6);
    Assert.assertArrayEquals(new byte[]{'[', '1', '2', '3', '0', ':', 0, ']'}, SecurityUtility.extractCompatibilityHeader(in));
    Assert.assertArrayEquals(new byte[]{78, 79}, IOUtility.readBytes(in));
  }
}
