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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.scout.rt.platform.util.BomInputStreamReader.BOM_CHAR;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BomInputStreamReaderTest {

  private static final String UTF_16 = "UTF-16";
  private static final String UTF_32 = "UTF-32";
  private static final String UTF_32LE = "UTF-32LE";
  private static final String UTF_32BE = "UTF-32BE";

  private static final String EMPTY_STRING = "";
  private static final String BOM_STRING = "" + BOM_CHAR;
  private static final String BOM_TEXT_STRING = "" + BOM_CHAR + "Text";
  private static final String BOM_BOM_STRING = "" + BOM_CHAR + BOM_CHAR;
  private static final String BOM_BOM_TEXT_STRING = "" + BOM_CHAR + BOM_CHAR + "Text";
  private static final String BOM_TEXT_BOM_STRING = "" + BOM_CHAR + "Text" + BOM_CHAR;
  private static final String TEXT_BOM_STRING = "Text" + BOM_CHAR;
  private static final String TEXT_STRING = "Text";

  @Parameters(name = "{index}: {0} - {1}")
  public static Iterable<Object[]> testData() {
    // unicode character sets
    List<Charset> unicodeCharsets = new ArrayList<>();
    unicodeCharsets.add(UTF_8);
    addIfSupported(UTF_16, unicodeCharsets);
    unicodeCharsets.add(UTF_16BE);
    unicodeCharsets.add(UTF_16LE);
    addIfSupported(UTF_32, unicodeCharsets);
    addIfSupported(UTF_32BE, unicodeCharsets);
    addIfSupported(UTF_32LE, unicodeCharsets);

    // all character sets
    List<Charset> allCharsets = new ArrayList<>();
    allCharsets.addAll(unicodeCharsets);
    allCharsets.add(US_ASCII);
    allCharsets.add(ISO_8859_1);

    // collect test input vectors, elements consists of:
    //   test name
    //   input string
    //   expected string after reading
    //   whether input string contains unicode chars
    List<Object[]> inputVector = Arrays.asList(
        new Object[]{"<empty>", EMPTY_STRING, EMPTY_STRING, false},
        new Object[]{"Text", TEXT_STRING, TEXT_STRING, false},
        new Object[]{"BOM", BOM_STRING, EMPTY_STRING, true},
        new Object[]{"BOM-Text", BOM_TEXT_STRING, TEXT_STRING, true},
        new Object[]{"BOM-BOM", BOM_BOM_STRING, BOM_STRING, true},
        new Object[]{"BOM-BOM-Text", BOM_BOM_TEXT_STRING, BOM_TEXT_STRING, true},
        new Object[]{"BOM-Text-BOM", BOM_TEXT_BOM_STRING, TEXT_BOM_STRING, true});

    // build test data matrix
    List<Object[]> testData = new ArrayList<Object[]>();
    for (Charset charset : allCharsets) {
      for (Object[] iv : inputVector) {
        boolean requiresUnicode = (boolean) iv[3];
        if (!requiresUnicode || unicodeCharsets.contains(charset)) {
          testData.add(new Object[]{charset, iv[0], iv[1], iv[2]});
        }
      }
    }

    return testData;
  }

  protected static void addIfSupported(String charset, List<Charset> charsets) {
    if (Charset.isSupported(charset)) {
      charsets.add(Charset.forName(charset));
    }
  }

  private final Charset m_charset;
  private final String m_testString;
  private final String m_expectedString;

  public BomInputStreamReaderTest(Charset charset, String testName, String testString, String expectedString) {
    m_charset = charset;
    m_testString = testString;
    m_expectedString = expectedString;
  }

  @Test
  public void testRead() throws IOException {
    final byte[] data = m_testString.getBytes(m_charset);
    try (BomInputStreamReader in = new BomInputStreamReader(new ByteArrayInputStream(data), m_charset)) {
      for (int i = 0; i < m_expectedString.length(); i++) {
        assertEquals(String.format("char at position %d does not match", i), m_expectedString.charAt(i), in.read());
      }
    }
  }

  @Test
  public void testReadCharArray() throws IOException {
    final byte[] data = m_testString.getBytes(m_charset);

    // read all data at once
    try (BomInputStreamReader in = new BomInputStreamReader(new ByteArrayInputStream(data), m_charset)) {
      int expectedLength = m_expectedString.length();
      char[] cbuf = new char[expectedLength];
      assertEquals(expectedLength, in.read(cbuf));
      assertEquals(m_expectedString, String.valueOf(cbuf));
    }

    // read char by char
    try (BomInputStreamReader in = new BomInputStreamReader(new ByteArrayInputStream(data), m_charset)) {
      char[] cbuf = new char[1];
      for (int i = 0; i < m_expectedString.length(); i++) {
        assertEquals(1, in.read(cbuf));
        assertEquals(String.format("char at position %d does not match", i), m_expectedString.charAt(i), cbuf[0]);
      }
    }
  }

  @Test
  public void testReadCharArrayWithOffsetLen() throws IOException {
    doTestReadCharArrayWithOffsetLen(0);
    for (int i = 0; i <= m_expectedString.length(); i++) {
      doTestReadCharArrayWithOffsetLen(i);
    }
  }

  protected void doTestReadCharArrayWithOffsetLen(int offset) throws IOException {
    final byte[] data = m_testString.getBytes(m_charset);
    try (BomInputStreamReader in = new BomInputStreamReader(new ByteArrayInputStream(data), m_charset)) {
      int expectedLength = m_expectedString.length();
      char[] cbuf = new char[expectedLength];
      int remainingLength = expectedLength - offset;
      assertEquals(remainingLength, in.read(cbuf, offset, remainingLength));
      assertEquals(StringUtility.lpad(m_expectedString, "\0", offset + expectedLength).substring(0, expectedLength), String.valueOf(cbuf));
    }
  }

  @Test
  public void testSkip() throws IOException {
    doTestSkip(0);
    doTestSkip(1);
    doTestSkip(20);
  }

  protected void doTestSkip(int len) throws IOException {
    final byte[] data = m_testString.getBytes(m_charset);
    try (BomInputStreamReader in = new BomInputStreamReader(new ByteArrayInputStream(data), m_charset)) {
      assertEquals(Math.min(len, m_expectedString.length()), in.skip(len));
      for (int i = len; i < m_expectedString.length() - len; i++) {
        assertEquals(String.format("char at position %d does not match", i), m_expectedString.charAt(i), in.read());
      }
    }
  }
}
