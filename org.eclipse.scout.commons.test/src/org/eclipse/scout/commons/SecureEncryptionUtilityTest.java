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
package org.eclipse.scout.commons;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class SecureEncryptionUtilityTest {

  private static final boolean IS_JAVA_18_OR_NEWER = CompareUtility.compareTo(System.getProperty("java.version"), "1.8") >= 0;
  private static final int KEY_LEN = 128;
  private static final String ENCODING = "UTF-8";
  private static final String PASSWORD = "insecure";

  @Test
  public void testEncryption() throws Exception {
    if (!IS_JAVA_18_OR_NEWER) {
      // encryption only supported in java 1.8 or newer
      return;
    }

    final String origData = "origData";
    final byte[] salt = EncryptionUtility2.createRandomBytes();
    final byte[] inputBytes = origData.getBytes(ENCODING);

    byte[] encryptData = EncryptionUtility2.encrypt(inputBytes, PASSWORD, salt, KEY_LEN);
    Assert.assertFalse(Arrays.equals(encryptData, inputBytes));

    byte[] encryptData2 = EncryptionUtility2.encrypt(null, PASSWORD, salt, KEY_LEN);
    Assert.assertNull(encryptData2);

    byte[] encryptData3 = EncryptionUtility2.encrypt(new byte[]{}, PASSWORD, salt, KEY_LEN);
    Assert.assertTrue(Arrays.equals(encryptData3, new byte[]{}));

    String decryptedString = new String(EncryptionUtility2.decrypt(encryptData, PASSWORD, salt, KEY_LEN), ENCODING);
    Assert.assertEquals(origData, decryptedString);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncryptNoSalt() throws Exception {
    EncryptionUtility2.encrypt("test".getBytes(ENCODING), "pass", null, KEY_LEN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncryptNoKey() throws Exception {
    final byte[] salt = EncryptionUtility2.createRandomBytes();
    EncryptionUtility2.encrypt("test".getBytes(ENCODING), null, salt, KEY_LEN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncryptWrongKeyLen() throws Exception {
    final byte[] salt = EncryptionUtility2.createRandomBytes();
    EncryptionUtility2.encrypt("test".getBytes(ENCODING), "pass", salt, 4);
  }

  @Test
  public void testRandomBytes() throws Exception {
    byte[] createRandomBytes = EncryptionUtility2.createRandomBytes();
    byte[] createRandomBytes2 = EncryptionUtility2.createRandomBytes();
    EncryptionUtility2.createRandomBytes(4);
    Assert.assertFalse(Arrays.equals(createRandomBytes, createRandomBytes2));

    // ensure wrong input generates exception
    boolean illegalExc = false;
    try {
      EncryptionUtility2.createRandomBytes(0);
    }
    catch (IllegalArgumentException e) {
      illegalExc = true;
    }
    Assert.assertTrue(illegalExc);
  }

  @Test
  public void testHash() throws Exception {
    final byte[] data = "testdata".getBytes("UTF-8");
    final byte[] salt = EncryptionUtility2.createRandomBytes();
    final byte[] salt2 = EncryptionUtility2.createRandomBytes();
    byte[] hash = EncryptionUtility2.hash(data, salt, 1);
    byte[] hash2 = EncryptionUtility2.hash(data, salt);
    byte[] hash3 = EncryptionUtility2.hash(data, salt2, 1);
    byte[] hash4 = EncryptionUtility2.hash(data, null, 1);
    byte[] hash5 = EncryptionUtility2.hash(data, null, 1);
    byte[] hash6 = EncryptionUtility2.hash(data, null);
    byte[] hash7 = EncryptionUtility2.hash(data, salt);
    byte[] hash8 = EncryptionUtility2.hash(new byte[]{}, salt);

    // ensure null input throws exception
    boolean nullNotAllowed = false;
    try {
      EncryptionUtility2.hash(null, salt);
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
}
