/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.eclipse.scout.commons.internal.tripledes.TripleDES;

/**
 * @deprecated This class in insecure and should no longer be used. Will be removed in Scout 7.0. Use
 *             {@link SecurityUtility} instead.
 */
@Deprecated
@SuppressWarnings("deprecation")
public final class EncryptionUtility {
  public static final byte[] DEFAULT_KEY = Base64Utility.decode("GQgTSBkIE0gZCBNIGQgTSBkIE0gZCBNI");// 24

  // bytes

  private EncryptionUtility() {
  }

  /**
   * @param data
   *          in base 64 format
   * @param tripleDesKey
   *          24 bytes triple-des key, the default {@link #DEFAULT_KEY} may also be used decrypt data using the
   *          triple-des key
   */
  public static byte[] decryptBase64(String data, byte[] tripleDesKey) {
    if (data == null) {
      return null;
    }
    if (data.length() >= 9 && data.charAt(8) == '#') {
      data = data.substring(9);
    }
    return decrypt(Base64Utility.decode(data), tripleDesKey);
  }

  /**
   * @param data
   * @param tripleDesKey
   *          24 bytes triple-des key, the default {@link #DEFAULT_KEY} may also be used decrypt data using the
   *          triple-des key
   */
  public static byte[] decrypt(byte[] data, byte[] tripleDesKey) {
    if (data == null) {
      return null;
    }
    TripleDES t = null;
    try {
      t = new TripleDES(tripleDesKey);
      return t.decrypt(data, true);
    }
    finally {
      if (t != null) {
        t.destroy();
      }
    }
  }

  /**
   * @param data
   * @param tripleDesKey
   *          24 bytes triple-des key, the default {@link #DEFAULT_KEY} may also be used
   * @return encrypted data using the triple-des key
   */
  public static byte[] encrypt(byte[] data, byte[] tripleDesKey) {
    if (data == null) {
      return null;
    }
    TripleDES t = null;
    try {
      t = new TripleDES(tripleDesKey);
      return t.encrypt(data);
    }
    finally {
      if (t != null) {
        t.destroy();
      }
    }
  }

  /**
   * @return data signed using MD5 algorithm
   */
  public static byte[] signMD5(byte[] data) throws NoSuchAlgorithmException {
    return MessageDigest.getInstance("MD5").digest(data);
  }

  /**
   * @return a new random 24 byte triple des key
   */
  public static byte[] createTripleDesKey() {
    Random r = new Random();
    byte[] key = new byte[24];
    r.nextBytes(key);
    return key;
  }
}
