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

public final class Base64Utility {

  private Base64Utility() {
  }

  private static final String ALPHABET =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

  /**
   * Base-64 encodes the supplied block of data. Line wrapping is not applied on
   * output.
   * 
   * @param bytes
   *          The block of data that is to be Base-64 encoded.
   * @return A <code>String</code> containing the encoded data.
   */
  public static String encode(byte[] bytes) {
    int length = bytes.length;
    if (length == 0) {
      return "";
    }
    StringBuffer buffer =
        new StringBuffer((int) Math.ceil(length / 3d) * 4);
    int remainder = length % 3;
    length -= remainder;
    int block;
    int i = 0;
    while (i < length) {
      block = ((bytes[i++] & 0xff) << 16) | ((bytes[i++] & 0xff) << 8) |
          (bytes[i++] & 0xff);
      buffer.append(ALPHABET.charAt(block >>> 18));
      buffer.append(ALPHABET.charAt((block >>> 12) & 0x3f));
      buffer.append(ALPHABET.charAt((block >>> 6) & 0x3f));
      buffer.append(ALPHABET.charAt(block & 0x3f));
    }
    if (remainder == 0) {
      return buffer.toString();
    }
    if (remainder == 1) {
      block = (bytes[i] & 0xff) << 4;
      buffer.append(ALPHABET.charAt(block >>> 6));
      buffer.append(ALPHABET.charAt(block & 0x3f));
      buffer.append("==");
      return buffer.toString();
    }
    block = (((bytes[i++] & 0xff) << 8) | ((bytes[i]) & 0xff)) << 2;
    buffer.append(ALPHABET.charAt(block >>> 12));
    buffer.append(ALPHABET.charAt((block >>> 6) & 0x3f));
    buffer.append(ALPHABET.charAt(block & 0x3f));
    buffer.append("=");
    return buffer.toString();
  }

  /**
   * Decodes the supplied Base-64 encoded string.
   * 
   * @param string
   *          The Base-64 encoded string that is to be decoded.
   * @return A <code>byte[]</code> containing the decoded data block.
   */
  public static byte[] decode(String string) {
    string = string.replaceAll("[^A-Za-z0-9+/=]", "");
    int length = string.length();
    if (length == 0) {
      return new byte[0];
    }
    int pad = (string.charAt(length - 2) == '=') ? 2 :
        (string.charAt(length - 1) == '=') ? 1 : 0;
    int size = length * 3 / 4 - pad;
    byte[] buffer = new byte[size];
    int block;
    int i = 0;
    int index = 0;
    while (i < length) {
      block = (ALPHABET.indexOf(string.charAt(i++)) & 0xff) << 18 |
          (ALPHABET.indexOf(string.charAt(i++)) & 0xff) << 12 |
          (ALPHABET.indexOf(string.charAt(i++)) & 0xff) << 6 |
          (ALPHABET.indexOf(string.charAt(i++)) & 0xff);
      buffer[index++] = (byte) (block >>> 16);
      if (index < size) {
        buffer[index++] = (byte) ((block >>> 8) & 0xff);
      }
      if (index < size) {
        buffer[index++] = (byte) (block & 0xff);
      }
    }
    return buffer;
  }

}
