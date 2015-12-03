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

/**
 * Uses 3 x 64 = 192 bit key DES
 *
 * @deprecated This class in insecure and should no longer be used. Will be removed in Scout 7.0. Use
 *             {@link SecurityUtility} instead.
 */
@Deprecated
public final class TripleDES {
  private DESKey k1;
  private DESKey k2;
  private DESKey k3;

  public TripleDES(byte[] key192Bit) {
    if (key192Bit.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Creating a zero-length triple-DES key");
    }
    if (key192Bit.length < 24) {
      byte[] k = new byte[24];
      for (int i = 0; i < 24; i += key192Bit.length) {
        System.arraycopy(key192Bit, 0, k, i, Math.min(key192Bit.length, 24 - i));
      }
      key192Bit = k;
    }
    k1 = new DESKey(makeLong(key192Bit, 0, 8));
    k2 = new DESKey(makeLong(key192Bit, 8, 8));
    k3 = new DESKey(makeLong(key192Bit, 16, 8));
  }

  public byte[] encrypt(byte[] plain) {
    if (plain == null || plain.length == 0) {
      return new byte[0];
    }
    int len = plain.length;
    byte[] cipher = new byte[((len + plainBlockSize() - 1) / plainBlockSize()) * plainBlockSize()];
    for (int i = 0; i < cipher.length; i = i + plainBlockSize()) {
      if (i + plainBlockSize() <= plain.length) {
        encryptBlock(plain, i, cipher, i);
      }
      else {
        // temporary array
        byte[] tmp = new byte[plainBlockSize()];
        System.arraycopy(plain, i, tmp, 0, plain.length - i);
        encryptBlock(tmp, 0, cipher, i);
      }
    }
    return cipher;
  }

  public byte[] decrypt(byte[] cipher) {
    return decrypt(cipher, false);
  }

  public byte[] decrypt(byte[] cipher, boolean removeEndingZeros) {
    if (cipher == null || cipher.length == 0) {
      return new byte[0];
    }
    int len = cipher.length;
    if (len % plainBlockSize() != 0) {
      throw new IllegalArgumentException("cipher length must be multiple of " + plainBlockSize());
    }
    byte[] plain = new byte[len];
    for (int i = 0; i < cipher.length; i = i + plainBlockSize()) {
      decryptBlock(cipher, i, plain, i);
    }
    if (removeEndingZeros) {
      int reducedLen = len;
      while (reducedLen > 0 && plain[reducedLen - 1] == 0x00) {
        reducedLen--;
      }
      if (reducedLen < len) {
        byte[] newPlain = new byte[reducedLen];
        System.arraycopy(plain, 0, newPlain, 0, reducedLen);
        plain = newPlain;
      }
    }
    return plain;
  }

  public void destroy() {
    if (k1 != null) {
      k1.destroy();
    }
    k1 = null;
    if (k2 != null) {
      k2.destroy();
    }
    k2 = null;
    if (k3 != null) {
      k3.destroy();
    }
    k3 = null;
  }

  private int plainBlockSize() {
    return 8;
  }

  private static long pickBits(long a, byte[] bits) {
    long r = 0;
    int l = bits.length;
    for (int b = 0; b < l; b++) {
      r = (r << 1) | ((a >>> (63 - bits[b])) & 1);
    }
    return r;
  }

  private void encryptBlock(byte[] source, int i, byte[] dest, int j) {
    long block = makeLong(source, i, 8);
    block = pickBits(block, DESKey.IP);
    block = k1.subCrypt(block);
    block = k2.subDecrypt(block);
    block = k3.subCrypt(block);
    block = pickBits(block, DESKey.FP);
    writeBytes(block, dest, j, 8);
  }

  private void decryptBlock(byte[] source, int i, byte[] dest, int j) {
    long block = makeLong(source, i, 8);
    block = pickBits(block, DESKey.IP);
    block = k3.subDecrypt(block);
    block = k2.subCrypt(block);
    block = k1.subDecrypt(block);
    block = pickBits(block, DESKey.FP);
    writeBytes(block, dest, j, 8);
  }

  private static void writeBytes(long a, byte[] dest, int i, int length) {
    for (int j = i + length - 1; j >= i; j--) {
      dest[j] = (byte) a;
      a = a >>> 8;
    }
  }

  private static long makeLong(byte[] buf, int i, int length) {
    long r = 0;
    length += i;
    for (int j = i; j < length; j++) {
      r = (r << 8) | (buf[j] & 0xffL);
    }
    return r;
  }

  /*
   * 64 bit key
   */
  private static class DESKey {
    private long key;
    private long[] subKeys;

    protected DESKey(long key) {
      this.key = key;
      buildSubKeys();
    }

    protected void destroy() {
      key = 0;
      if (subKeys != null && subKeys.length > 0) {
        for (int i = 0; i < subKeys.length; i++) {
          subKeys[i] = 0;
        }
      }
      subKeys = null;
    }

    protected long subCrypt(long block) {
      int i = (int) (block >>> 32);
      int r = (int) block;
      for (int k = 0; k < 16; k++) {
        int t = i;
        i = r;
        r = t ^ f(r, subKeys[k]);
      }
      return ((long) r << 32) | (i & 0xffffffffL);
    }

    protected long subDecrypt(long block) {
      int i = (int) (block >>> 32);
      int r = (int) block;
      for (int k = 15; k >= 0; k--) {
        int t = i;
        i = r;
        r = t ^ f(r, subKeys[k]);
      }
      return ((long) r << 32) | (i & 0xffffffffL);
    }

    private void buildSubKeys() {
      long k = pickBits(key, PC1);
      subKeys = new long[16];
      for (int i = 0; i < 16; i++) {
        if ((i == 0) || (i == 1) || (i == 8) || (i == 15)) {
          k = ((k << 1) & 0xffffffeffffffeL) | ((k >>> 27) & 0x00000010000001L);
        }
        else {
          k = ((k << 2) & 0xffffffcffffffcL) | ((k >>> 26) & 0x00000030000003L);
        }
        subKeys[i] = pickBits(k, PC2);
      }
    }

    /**
     * TRIPLE*DES MATRIX
     */
    private static final byte[] PC1 =
        {56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 60, 52, 44, 36, 28, 20, 12, 4, 27, 19, 11, 3};

    private static final byte[] PC2 = {21, 24, 18, 31, 8, 12, 10, 35, 22, 13, 28, 17, 30, 26, 19, 11, 33, 15, 23, 14, 34, 27, 20, 9, 48, 59, 38, 44, 54, 62, 37, 47, 58, 52, 40, 55, 51, 56, 46, 63, 41, 60, 53, 49, 57, 43, 36, 39};

    private static final int[] S1 = {0x00808200, 0x00000000, 0x00008000, 0x00808202, 0x00808002, 0x00008202, 0x00000002, 0x00008000, 0x00000200, 0x00808200, 0x00808202, 0x00000200, 0x00800202, 0x00808002, 0x00800000, 0x00000002, 0x00000202,
        0x00800200, 0x00800200, 0x00008200, 0x00008200, 0x00808000, 0x00808000, 0x00800202, 0x00008002, 0x00800002, 0x00800002, 0x00008002, 0x00000000, 0x00000202, 0x00008202, 0x00800000, 0x00008000, 0x00808202, 0x00000002, 0x00808000,
        0x00808200, 0x00800000, 0x00800000, 0x00000200, 0x00808002, 0x00008000, 0x00008200, 0x00800002, 0x00000200, 0x00000002, 0x00800202, 0x00008202, 0x00808202, 0x00008002, 0x00808000, 0x00800202, 0x00800002, 0x00000202, 0x00008202,
        0x00808200, 0x00000202, 0x00800200, 0x00800200, 0x00000000, 0x00008002, 0x00008200, 0x00000000, 0x00808002};

    private static final int[] S2 = {0x40084010, 0x40004000, 0x00004000, 0x00084010, 0x00080000, 0x00000010, 0x40080010, 0x40004010, 0x40000010, 0x40084010, 0x40084000, 0x40000000, 0x40004000, 0x00080000, 0x00000010, 0x40080010, 0x00084000,
        0x00080010, 0x40004010, 0x00000000, 0x40000000, 0x00004000, 0x00084010, 0x40080000, 0x00080010, 0x40000010, 0x00000000, 0x00084000, 0x00004010, 0x40084000, 0x40080000, 0x00004010, 0x00000000, 0x00084010, 0x40080010, 0x00080000,
        0x40004010, 0x40080000, 0x40084000, 0x00004000, 0x40080000, 0x40004000, 0x00000010, 0x40084010, 0x00084010, 0x00000010, 0x00004000, 0x40000000, 0x00004010, 0x40084000, 0x00080000, 0x40000010, 0x00080010, 0x40004010, 0x40000010,
        0x00080010, 0x00084000, 0x00000000, 0x40004000, 0x00004010, 0x40000000, 0x40080010, 0x40084010, 0x00084000};

    private static final int[] S3 = {0x00000104, 0x04010100, 0x00000000, 0x04010004, 0x04000100, 0x00000000, 0x00010104, 0x04000100, 0x00010004, 0x04000004, 0x04000004, 0x00010000, 0x04010104, 0x00010004, 0x04010000, 0x00000104, 0x04000000,
        0x00000004, 0x04010100, 0x00000100, 0x00010100, 0x04010000, 0x04010004, 0x00010104, 0x04000104, 0x00010100, 0x00010000, 0x04000104, 0x00000004, 0x04010104, 0x00000100, 0x04000000, 0x04010100, 0x04000000, 0x00010004, 0x00000104,
        0x00010000, 0x04010100, 0x04000100, 0x00000000, 0x00000100, 0x00010004, 0x04010104, 0x04000100, 0x04000004, 0x00000100, 0x00000000, 0x04010004, 0x04000104, 0x00010000, 0x04000000, 0x04010104, 0x00000004, 0x00010104, 0x00010100,
        0x04000004, 0x04010000, 0x04000104, 0x00000104, 0x04010000, 0x00010104, 0x00000004, 0x04010004, 0x00010100};

    private static final int[] S4 = {0x80401000, 0x80001040, 0x80001040, 0x00000040, 0x00401040, 0x80400040, 0x80400000, 0x80001000, 0x00000000, 0x00401000, 0x00401000, 0x80401040, 0x80000040, 0x00000000, 0x00400040, 0x80400000, 0x80000000,
        0x00001000, 0x00400000, 0x80401000, 0x00000040, 0x00400000, 0x80001000, 0x00001040, 0x80400040, 0x80000000, 0x00001040, 0x00400040, 0x00001000, 0x00401040, 0x80401040, 0x80000040, 0x00400040, 0x80400000, 0x00401000, 0x80401040,
        0x80000040, 0x00000000, 0x00000000, 0x00401000, 0x00001040, 0x00400040, 0x80400040, 0x80000000, 0x80401000, 0x80001040, 0x80001040, 0x00000040, 0x80401040, 0x80000040, 0x80000000, 0x00001000, 0x80400000, 0x80001000, 0x00401040,
        0x80400040, 0x80001000, 0x00001040, 0x00400000, 0x80401000, 0x00000040, 0x00400000, 0x00001000, 0x00401040};

    private static final int[] S5 = {0x00000080, 0x01040080, 0x01040000, 0x21000080, 0x00040000, 0x00000080, 0x20000000, 0x01040000, 0x20040080, 0x00040000, 0x01000080, 0x20040080, 0x21000080, 0x21040000, 0x00040080, 0x20000000, 0x01000000,
        0x20040000, 0x20040000, 0x00000000, 0x20000080, 0x21040080, 0x21040080, 0x01000080, 0x21040000, 0x20000080, 0x00000000, 0x21000000, 0x01040080, 0x01000000, 0x21000000, 0x00040080, 0x00040000, 0x21000080, 0x00000080, 0x01000000,
        0x20000000, 0x01040000, 0x21000080, 0x20040080, 0x01000080, 0x20000000, 0x21040000, 0x01040080, 0x20040080, 0x00000080, 0x01000000, 0x21040000, 0x21040080, 0x00040080, 0x21000000, 0x21040080, 0x01040000, 0x00000000, 0x20040000,
        0x21000000, 0x00040080, 0x01000080, 0x20000080, 0x00040000, 0x00000000, 0x20040000, 0x01040080, 0x20000080};

    private static final int[] S6 = {0x10000008, 0x10200000, 0x00002000, 0x10202008, 0x10200000, 0x00000008, 0x10202008, 0x00200000, 0x10002000, 0x00202008, 0x00200000, 0x10000008, 0x00200008, 0x10002000, 0x10000000, 0x00002008, 0x00000000,
        0x00200008, 0x10002008, 0x00002000, 0x00202000, 0x10002008, 0x00000008, 0x10200008, 0x10200008, 0x00000000, 0x00202008, 0x10202000, 0x00002008, 0x00202000, 0x10202000, 0x10000000, 0x10002000, 0x00000008, 0x10200008, 0x00202000,
        0x10202008, 0x00200000, 0x00002008, 0x10000008, 0x00200000, 0x10002000, 0x10000000, 0x00002008, 0x10000008, 0x10202008, 0x00202000, 0x10200000, 0x00202008, 0x10202000, 0x00000000, 0x10200008, 0x00000008, 0x00002000, 0x10200000,
        0x00202008, 0x00002000, 0x00200008, 0x10002008, 0x00000000, 0x10202000, 0x10000000, 0x00200008, 0x10002008};

    private static final int[] S7 = {0x00100000, 0x02100001, 0x02000401, 0x00000000, 0x00000400, 0x02000401, 0x00100401, 0x02100400, 0x02100401, 0x00100000, 0x00000000, 0x02000001, 0x00000001, 0x02000000, 0x02100001, 0x00000401, 0x02000400,
        0x00100401, 0x00100001, 0x02000400, 0x02000001, 0x02100000, 0x02100400, 0x00100001, 0x02100000, 0x00000400, 0x00000401, 0x02100401, 0x00100400, 0x00000001, 0x02000000, 0x00100400, 0x02000000, 0x00100400, 0x00100000, 0x02000401,
        0x02000401, 0x02100001, 0x02100001, 0x00000001, 0x00100001, 0x02000000, 0x02000400, 0x00100000, 0x02100400, 0x00000401, 0x00100401, 0x02100400, 0x00000401, 0x02000001, 0x02100401, 0x02100000, 0x00100400, 0x00000000, 0x00000001,
        0x02100401, 0x00000000, 0x00100401, 0x02100000, 0x00000400, 0x02000001, 0x02000400, 0x00000400, 0x00100001};

    private static final int[] S8 = {0x08000820, 0x00000800, 0x00020000, 0x08020820, 0x08000000, 0x08000820, 0x00000020, 0x08000000, 0x00020020, 0x08020000, 0x08020820, 0x00020800, 0x08020800, 0x00020820, 0x00000800, 0x00000020, 0x08020000,
        0x08000020, 0x08000800, 0x00000820, 0x00020800, 0x00020020, 0x08020020, 0x08020800, 0x00000820, 0x00000000, 0x00000000, 0x08020020, 0x08000020, 0x08000800, 0x00020820, 0x00020000, 0x00020820, 0x00020000, 0x08020800, 0x00000800,
        0x00000020, 0x08020020, 0x00000800, 0x00020820, 0x08000800, 0x00000020, 0x08000020, 0x08020000, 0x08020020, 0x08000000, 0x00020000, 0x08000820, 0x00000000, 0x08020820, 0x00020020, 0x08000020, 0x08020000, 0x08000800, 0x08000820,
        0x00000000, 0x08020820, 0x00020800, 0x00020800, 0x00000820, 0x00000820, 0x00020020, 0x08000000, 0x08020800};

    protected static final byte[] IP = {57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7, 56, 48, 40, 32, 24, 16, 8, 0, 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36,
        28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6,};

    protected static final byte[] FP = {39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9,
        49, 17, 57, 25, 32, 0, 40, 8, 48, 16, 56, 24};

    private static int f(int r, long k) {
      return S1[(int) ((((r << 5) & 0x20) | ((r >>> 27) & 0x1f)) ^ ((k >>> 42) & 0x3f))] | S2[(int) (((r >>> 23) & 0x3f) ^ ((k >>> 36) & 0x3f))] | S3[(int) (((r >>> 19) & 0x3f) ^ ((k >>> 30) & 0x3f))]
          | S4[(int) (((r >>> 15) & 0x3f) ^ ((k >>> 24) & 0x3f))] | S5[(int) (((r >>> 11) & 0x3f) ^ ((k >>> 18) & 0x3f))] | S6[(int) (((r >>> 7) & 0x3f) ^ ((k >>> 12) & 0x3f))] | S7[(int) (((r >>> 3) & 0x3f) ^ ((k >>> 6) & 0x3f))]
          | S8[(int) ((((r >>> 31) & 0x01) | ((r << 1) & 0x3e)) ^ (k & 0x3f))];
    }
  }
}
