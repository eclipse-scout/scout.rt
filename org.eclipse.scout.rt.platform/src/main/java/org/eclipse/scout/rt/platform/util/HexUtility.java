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
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @since 5.1
 */
public final class HexUtility {

  private HexUtility() {
  }

  private static final char[] BYTE_TO_CHAR_LOWER;
  private static final char[] BYTE_TO_CHAR_UPPER;
  private static final int[] CHAR_TO_BYTE;

  static {
    BYTE_TO_CHAR_LOWER = "0123456789abcdef".toCharArray();
    BYTE_TO_CHAR_UPPER = "0123456789ABCDEF".toCharArray();

    int[] charToByte = new int[256];
    for (int i = 0; i < charToByte.length; i++) {
      charToByte[i] = -1;
    }
    for (int i = 0; i < BYTE_TO_CHAR_LOWER.length; i++) {
      charToByte[(int) BYTE_TO_CHAR_LOWER[i]] = i;
    }
    for (int i = 0; i < BYTE_TO_CHAR_UPPER.length; i++) {
      charToByte[(int) BYTE_TO_CHAR_UPPER[i]] = i;
    }
    CHAR_TO_BYTE = charToByte;
  }

  /**
   * Hex encodes the supplied block of data. Line wrapping is not applied on output.
   *
   * @param bytes
   *          The block of data that is to be Hex encoded.
   * @return A <code>String</code> containing the encoded data.
   */
  public static String encode(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    int length = bytes.length;
    if (length == 0) {
      return "";
    }
    StringWriter out = new StringWriter(length * 2);
    try (HexOutputStream h = new HexOutputStream(out)) {
      h.write(bytes);
      h.close();
    }
    catch (IOException e) {
      throw new RuntimeException("Unexpected behaviour", e);
    }
    return out.toString();
  }

  /**
   * Decodes the supplied Hex encoded string.
   *
   * @param hex
   *          The Hex encoded string that is to be decoded.
   * @return A <code>byte[]</code> containing the decoded data block.
   */
  public static byte[] decode(String hex) {
    if (hex == null) {
      return null;
    }
    int length = hex.length();
    if (length == 0) {
      return new byte[0];
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream(length / 2);
    try (HexInputStream h = new HexInputStream(new StringReader(hex))) {
      int b;
      while ((b = h.read()) >= 0) {
        out.write(b);
      }
      out.close();
    }
    catch (IOException e) {
      throw new RuntimeException("Unexpected behaviour", e);
    }
    return out.toByteArray();
  }

  /**
   * convert a hex encoded stream to byte stream
   */
  public static class HexInputStream extends InputStream {
    private final InputStream m_in0;
    private final Reader m_in1;

    public HexInputStream(Reader in) {
      m_in0 = null;
      m_in1 = in;
    }

    public HexInputStream(InputStream in) {
      m_in0 = in;
      m_in1 = null;
    }

    @Override
    public int read() throws IOException {
      int hi = readNext4Bits();
      if (hi < 0) {
        return -1;
      }
      int lo = readNext4Bits();
      if (lo < 0) {
        return -1;
      }
      return (hi << 4) | lo;
    }

    protected int readNext4Bits() throws IOException {
      int ch;
      while (true) {
        ch = (m_in0 != null ? m_in0.read() : m_in1.read());
        if (ch < 0) {
          //eof
          return -1;
        }
        ch = CHAR_TO_BYTE[ch];
        if (ch < 0) {
          //skip
        }
        else {
          return ch;
        }
      }
    }

    @Override
    public void close() throws IOException {
      if (m_in0 != null) {
        m_in0.close();
      }
      if (m_in1 != null) {
        m_in1.close();
      }
    }
  }

  /**
   * convert a byte stream to a hex encoded stream
   */
  public static class HexOutputStream extends OutputStream {
    private final OutputStream m_out0;
    private final Writer m_out1;

    public HexOutputStream(OutputStream out) {
      m_out0 = out;
      m_out1 = null;
    }

    public HexOutputStream(Writer out) {
      m_out0 = null;
      m_out1 = out;
    }

    @Override
    public void write(int b) throws IOException {
      if (m_out0 != null) {
        m_out0.write(BYTE_TO_CHAR_LOWER[(b >> 4) & 0x0f]);
        m_out0.write(BYTE_TO_CHAR_LOWER[b & 0x0f]);
      }
      else {
        m_out1.write(BYTE_TO_CHAR_LOWER[(b >> 4) & 0x0f]);
        m_out1.write(BYTE_TO_CHAR_LOWER[b & 0x0f]);
      }
    }

    @Override
    public void flush() throws IOException {
      if (m_out0 != null) {
        m_out0.flush();
      }
      else {
        m_out1.flush();
      }
    }

    @Override
    public void close() throws IOException {
      if (m_out0 != null) {
        m_out0.close();
      }
      else {
        m_out1.close();
      }
    }
  }

}
