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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Input stream reader that detects and removes a possibly available byte order mark (BOM) at the beginning of a stream.
 * The BOM processing is done for the following unicode character sets (i.e. encodings):
 * <ul>
 * <li>UTF-8</li>
 * <li>UTF-16</li>
 * <li>UTF-16BE</li>
 * <li>UTF-16LE</li>
 * </ul>
 * The following unicode character sets are already handled correctly by the JRE:
 * <ul>
 * <li>UTF-32</li>
 * <li>UTF-32BE</li>
 * <li>UTF-32LE</li>
 * </ul>
 * The processing of other character sets are still supported and they are not affected by this implementation. <br/>
 * <b>Note:</b> This implementation does not auto-detect neither the encoding nor the endianness (the information is
 * provided by a constructor argument).
 *
 * @since 5.2
 */
public class BomInputStreamReader extends InputStreamReader {

  /**
   * Unicode byte order mark that may appear at the beginning of a unicode byte stream. The representation of the BOM
   * differs only in certain encodings. But the unicode character code remains the same.
   *
   * @see <a href="http://www.unicode.org/faq/utf_bom.html#BOM">Unicode FAQ about BOM</a>
   */
  public static final char BOM_CHAR = '\ufeff';

  private static final Set<String> SUPPORTED_UNICODE_CHARSETS;
  private static final int EOF = -1;

  static {
    // setup set of unicode character sets handled by this implementation
    Charset[] cs = new Charset[]{
        StandardCharsets.UTF_8,
        StandardCharsets.UTF_16,
        StandardCharsets.UTF_16BE,
        StandardCharsets.UTF_16LE};

    Set<String> supportedCharsets = new HashSet<>();
    for (Charset charset : cs) {
      supportedCharsets.add(charset.name());
      supportedCharsets.addAll(charset.aliases());
    }

    SUPPORTED_UNICODE_CHARSETS = supportedCharsets;
  }

  boolean m_firstChar = true;

  public BomInputStreamReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
    super(in, charsetName);
  }

  public BomInputStreamReader(InputStream in, Charset cs) {
    super(in, cs);
  }

  public BomInputStreamReader(InputStream in, CharsetDecoder dec) {
    super(in, dec);
  }

  @Override
  public int read() throws IOException {
    int ch = super.read();
    if (m_firstChar) {
      // check if first char is the BOM
      m_firstChar = false;
      if (ch == BOM_CHAR && SUPPORTED_UNICODE_CHARSETS.contains(getEncoding())) {
        // first char is the BOM -> read and return next char
        ch = super.read();
      }
    }
    return ch;
  }

  @Override
  public int read(char[] cbuf, int offset, int length) throws IOException {
    if (length == 0) {
      // delegate to default behavior if no chars need to be read
      return super.read(cbuf, offset, length);
    }
    if (m_firstChar) {
      // check if first char is the BOM
      m_firstChar = false;
      if (SUPPORTED_UNICODE_CHARSETS.contains(getEncoding())) {
        int ch = super.read();
        if (ch == EOF) {
          return EOF;
        }
        else if (ch != BOM_CHAR) {
          // first char is not the BOM. Read remaining chars
          cbuf[offset] = (char) ch;
          return super.read(cbuf, offset + 1, length - 1) + 1;
        }
      }
    }
    return super.read(cbuf, offset, length);
  }
}
