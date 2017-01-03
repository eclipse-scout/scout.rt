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
package org.eclipse.scout.rt.platform.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Base64Utility {
  private static final Logger LOG = LoggerFactory.getLogger(Base64Utility.class);

  private Base64Utility() {
  }

  private static final char[] BYTE_TO_CHAR;
  private static final char[] BYTE_TO_CHAR_URL_SAFE;
  private static final int[] CHAR_TO_BYTE;
  private static final int[] CHAR_TO_BYTE_URL_SAFE;

  static {
    BYTE_TO_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    BYTE_TO_CHAR_URL_SAFE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

    int[] charToByte = new int[128];
    for (int i = 0; i < BYTE_TO_CHAR.length; i++) {
      charToByte[(int) BYTE_TO_CHAR[i]] = i;
    }
    CHAR_TO_BYTE = charToByte;

    charToByte = new int[128];
    for (int i = 0; i < BYTE_TO_CHAR_URL_SAFE.length; i++) {
      charToByte[(int) BYTE_TO_CHAR_URL_SAFE[i]] = i;
    }
    CHAR_TO_BYTE_URL_SAFE = charToByte;
  }

  /**
   * Calls {@link #encode(byte[], boolean)} with false
   */
  public static String encode(byte[] bytes) {
    return encode(bytes, false);
  }

  /**
   * Calls {@link #encode(byte[], boolean)} with true
   */
  public static String encodeUrlSafe(byte[] bytes) {
    return encode(bytes, true);
  }

  /**
   * Base-64 encodes the supplied block of data. Line wrapping is not applied on output.
   *
   * @param bytes
   *          The block of data that is to be Base-64 encoded.
   * @param urlSafe
   *          The alphabet used for the regular base 64 encoding contains '/' and '+' which are not allowed in URLs.
   *          With this property set to true, these chars are replaced with '-' and '_' so that the generated string may
   *          be used for URLs. See also
   *          <a href="https://tools.ietf.org/html/rfc4648#page-7">https://tools.ietf.org/html/rfc4648</a>.
   * @return A <code>String</code> containing the encoded data.
   */
  public static String encode(byte[] bytes, boolean urlSafe) {
    int length = bytes.length;
    if (length == 0) {
      return "";
    }
    char[] mappingTable;
    if (urlSafe) {
      mappingTable = BYTE_TO_CHAR_URL_SAFE;
    }
    else {
      mappingTable = BYTE_TO_CHAR;
    }
    StringBuilder buffer = new StringBuilder((int) Math.ceil(length / 3d) * 4);
    int remainder = length % 3;
    length -= remainder;
    int block;
    int i = 0;
    while (i < length) {
      block = ((bytes[i++] & 0xff) << 16) | ((bytes[i++] & 0xff) << 8)
          | (bytes[i++] & 0xff);
      buffer.append(mappingTable[block >>> 18]);
      buffer.append(mappingTable[(block >>> 12) & 0x3f]);
      buffer.append(mappingTable[(block >>> 6) & 0x3f]);
      buffer.append(mappingTable[block & 0x3f]);
    }
    if (remainder == 0) {
      return buffer.toString();
    }
    if (remainder == 1) {
      block = (bytes[i] & 0xff) << 4;
      buffer.append(mappingTable[block >>> 6]);
      buffer.append(mappingTable[block & 0x3f]);
      buffer.append("==");
      return buffer.toString();
    }
    block = (((bytes[i++] & 0xff) << 8) | ((bytes[i]) & 0xff)) << 2;
    buffer.append(mappingTable[block >>> 12]);
    buffer.append(mappingTable[(block >>> 6) & 0x3f]);
    buffer.append(mappingTable[block & 0x3f]);
    buffer.append("=");
    return buffer.toString();
  }

  /**
   * Calls {@link #decode(String, boolean)} with false.
   */
  public static byte[] decode(String string) {
    return decode(string, false);
  }

  /**
   * Calls {@link #decode(String, boolean)} with true.
   */
  public static byte[] decodeUrlSafe(String string) {
    return decode(string, true);
  }

  /**
   * Decodes the supplied Base-64 encoded string.
   *
   * @param string
   *          The Base-64 encoded string that is to be decoded.
   * @param urlSafe
   *          The alphabet used for the regular base 64 encoding contains '/' and '+' which are not allowed in URLs.
   *          With this property set to true, these chars are replaced with '-' and '_' so that the generated string may
   *          be used for URLs. See also
   *          <a href="https://tools.ietf.org/html/rfc4648#page-7">https://tools.ietf.org/html/rfc4648</a>.
   * @return A <code>byte[]</code> containing the decoded data block.
   */
  public static byte[] decode(String string, boolean urlSafe) {
    int length = string == null ? 0 : string.length();
    if (length == 0) {
      return new byte[0];
    }
    int[] mappingTable;
    if (urlSafe) {
      mappingTable = CHAR_TO_BYTE_URL_SAFE;
    }
    else {
      mappingTable = CHAR_TO_BYTE;
    }
    try (P_Base64InputStream is = new P_Base64InputStream(string, urlSafe)) {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int c1, c2, c3, c4;
      c1 = is.read();
      c2 = is.read();
      c3 = is.read();
      c4 = is.read();
      while (c1 >= 0 || c2 >= 0 || c3 >= 0 || c4 >= 0) {
        int block;
        block = ((c1 != -1 ? mappingTable[c1] : -1) & 0xff) << 18
            | ((c2 != -1 ? mappingTable[c2] : -1) & 0xff) << 12
            | ((c3 != -1 ? mappingTable[c3] : -1) & 0xff) << 6
            | ((c4 != -1 ? mappingTable[c4] : -1) & 0xff);
        buffer.write((byte) (block >>> 16));
        if (c3 != -1) {
          buffer.write((byte) ((block >>> 8) & 0xff));
        }
        if (c4 != -1) {
          buffer.write((byte) (block & 0xff));
        }
        c1 = is.read();
        c2 = is.read();
        c3 = is.read();
        c4 = is.read();
      }
      return buffer.toByteArray();
    }
    catch (IOException e) {
      LOG.error("IOException in Base64Utility.decode()", e);
      return new byte[0];
    }
  }

  private static class P_Base64InputStream extends InputStream {
    private final String m_buffer;
    private final int m_count;
    private final boolean m_urlSafe;
    private int m_pos = 0;

    public P_Base64InputStream(String base64String, boolean urlSafe) {
      m_buffer = base64String;
      m_count = base64String.length();
      m_urlSafe = urlSafe;
    }

    protected boolean matchesUrlChars(char ch) {
      if (m_urlSafe) {
        return (ch == '-') || (ch == '_');
      }
      return (ch == '+') || (ch == '/');
    }

    @Override
    public int read() throws IOException {
      while (m_pos < m_count) {
        char ch = m_buffer.charAt(m_pos++);
        if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || matchesUrlChars(ch)) {
          return (ch & 0xFF);
        }
      }
      return -1;
    }
  }
}
