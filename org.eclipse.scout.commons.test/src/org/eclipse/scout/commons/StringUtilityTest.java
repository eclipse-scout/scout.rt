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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * JUnit tests for {@link StringUtility}
 */
public class StringUtilityTest {

  @Test
  public void testImplodeDelimiters() {
    String a = "a";
    String b = "b";
    String c = "cccccc";
    String del1 = ";";
    String del2 = ",";
    String del3 = "";
    String delnil = null;
    String res1 = StringUtility.join(del1, a, b, c);
    assertEquals(a + del1 + b + del1 + c, res1);
    String res2 = StringUtility.join(del2, a, b, c);
    assertEquals(a + del2 + b + del2 + c, res2);
    String res3 = StringUtility.join(del3, a, b, c);
    assertEquals(a + b + c, res3);
    String res4 = StringUtility.join(delnil, a, b, c);
    assertEquals(a + b + c, res4);
  }

  @Test
  public void testImplodeNullInput() {
    String a = "a";
    String b = "b";
    String c = "cccccc";
    String del1 = ";";
    String res1 = StringUtility.join(del1);
    assertEquals("", res1);
    String res2 = StringUtility.join(del1, (String) null);
    assertEquals("", res2);
    String res3 = StringUtility.join(del1, a, (String) null, c);
    assertEquals(a + del1 + c, res3);
    String res4 = StringUtility.join(del1, (String) null, b, c);
    assertEquals(b + del1 + c, res4);
  }

  @Test
  public void testImplodeLong() {
    Long[] longs1 = new Long[]{15L, 4L};
    Long[] longs2 = new Long[]{};
    Long[] longs3 = new Long[]{34L, 340283503853L, null, -3431L};
    Long[] longs4 = new Long[]{null};
    String del1 = "'";
    String res1 = StringUtility.join(del1, longs1);
    assertEquals("15'4", res1);
    String res2 = StringUtility.join(del1, longs2);
    assertEquals("", res2);
    String res3 = StringUtility.join(del1, longs3);
    assertEquals("34'340283503853'-3431", res3);
    String res4 = StringUtility.join(del1, longs4);
    assertEquals("", res4);
  }

  @Test
  public void testJoinVararg() {
    assertEquals("", StringUtility.join(null));
    assertEquals("ab", StringUtility.join(null, "a", "b"));
    assertEquals("ab", StringUtility.join(null, "a", null, "b"));
    assertEquals("", StringUtility.join(null, (Object[]) null));
    assertEquals("1, true", StringUtility.join(", ", BigDecimal.ONE, Boolean.TRUE));
  }

  @Test
  public void testJoinStringArray() {
    assertEquals("", StringUtility.join(null, (String[]) null));
    assertEquals("ab", StringUtility.join(null, new String[]{"a", "b"}));
    assertEquals("a, b", StringUtility.join(", ", new String[]{"a", "b"}));
    assertEquals("a, b", StringUtility.join(", ", new String[]{"a", null, "b"}));
  }

  @Test
  public void testJoinLongArray() {
    assertEquals("", StringUtility.join(null, (Long[]) null));
    assertEquals("12", StringUtility.join(null, new Long[]{Long.valueOf(1), Long.valueOf(2)}));
    assertEquals("1, 2", StringUtility.join(", ", new Long[]{Long.valueOf(1), Long.valueOf(2)}));
    assertEquals("1, 2", StringUtility.join(", ", new Long[]{Long.valueOf(1), null, Long.valueOf(2)}));
  }

  @Test
  public void testMnemonics() {
    String s = "Button &Test";
    assertEquals('T', StringUtility.getMnemonic(s));
    assertEquals("Button Test", StringUtility.removeMnemonic(s));
    s = "Button & Test";
    assertEquals(0x00, StringUtility.getMnemonic(s));
    assertEquals(s, StringUtility.removeMnemonic(s));
    s = "&test";
    assertEquals('t', StringUtility.getMnemonic(s));
    assertEquals("test", StringUtility.removeMnemonic(s));
    s = "test &";
    assertEquals(0x00, StringUtility.getMnemonic(s));
    assertEquals(s, StringUtility.removeMnemonic(s));
  }

  // UTF-8 length is 13 to avoid accidental buffer size matches
  static final String CHARACTERS = "aouäöüàé";

  @Test
  public void testDecompress_umlauts() throws Exception {

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 100000; i++) {
      builder.append(CHARACTERS);
    }

    String original = builder.toString();
    String decompressed = StringUtility.decompress(StringUtility.compress(original));

    assertEquals(original, decompressed);
  }

  @Test
  public void testTags() throws Exception {
    String s;
    s = "foo <a>text</a> bar";
    assertEquals("foo  bar", StringUtility.removeTag(s, "a"));
    s = "foo <a>text</a> <a>text</a> bar";
    assertEquals("foo   bar", StringUtility.removeTag(s, "a"));
    s = "foo <a>text</a> <a>bar";
    assertEquals("foo  <a>bar", StringUtility.removeTag(s, "a"));
    s = "foo <a>text</a> </a>bar";
    assertEquals("foo  </a>bar", StringUtility.removeTag(s, "a"));
    s = "foo <a/> bar";
    assertEquals("foo  bar", StringUtility.removeTag(s, "a"));
  }

  @Test
  public void testRegExPattern() throws Exception {
    String s;
    s = "test*";
    assertEquals("test.*", StringUtility.toRegExPattern(s));

    s = "test?";
    assertEquals("test.", StringUtility.toRegExPattern(s));

    s = "com.test.*";
    assertEquals("com\\.test\\..*", StringUtility.toRegExPattern(s));
  }

  @Test
  public void testHtmlEncodeBackslash() {
    assertEqualsAfterEncodeDecode("\"");
  }

  @Test
  public void testHtmlEncodeAmp() {
    assertEqualsAfterEncodeDecode("&");
  }

  @Test
  public void testHtmlEncodeLt() {
    assertEqualsAfterEncodeDecode("<");
  }

  @Test
  public void testHtmlEncodeGt() {
    assertEqualsAfterEncodeDecode(">");
  }

  @Test
  public void testHtmlEncodeApostrophe() {
    assertEqualsAfterEncodeDecode("'");
  }

  @Test
  public void testHtmlEncodeBr() {
    assertEqualsAfterEncodeDecode("\n");
  }

  @Test
  public void testHtmlEncodeTab() {
    assertEqualsAfterEncodeDecode("\t");
  }

  @Test
  public void testHtmlEncodeNull() {
    assertEqualsAfterEncodeDecode(null);
  }

  @Test
  public void testHtmlEncodeEmpty() {
    assertEqualsAfterEncodeDecode("");
  }

  @Test
  public void testHtmlEncodeSpace() {
    assertEqualsAfterEncodeDecode("\\s", true);
  }

  @Test
  public void testHtmlEncodeDecodeHref() {
    String testHtml = "<li><a href=\"/home.html\" class=\"active\" title=\"Home\">Home</a></li>";
    assertEqualsAfterEncodeDecode(testHtml);
  }

  /**
   * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=347254
   */
  @Test
  public void testHtmlEncodeAmpLT() {
    String testHtml = "<a &lt; b>";
    assertEqualsAfterEncodeDecode(testHtml);
    String htmlDecode = StringUtility.htmlDecode("&lt;a &amp;lt; b&gt;");
    assertEquals("<a &lt; b>", htmlDecode);
  }

  /**
   * Tests if the result string is equal to the original after applying encode and decode.
   * 
   * @param original
   *          the original String
   */
  private static void assertEqualsAfterEncodeDecode(String original) {
    assertEqualsAfterEncodeDecode(original, false);
  }

  /**
   * Tests if the result string is equal to the original after applying encode and decode.
   * 
   * @param original
   *          the original String
   * @param replaceSpace
   *          replace all spaces when encoding
   */
  private static void assertEqualsAfterEncodeDecode(String original, boolean replaceSpaces) {
    String encoded = StringUtility.htmlEncode(original, replaceSpaces);
    String decoded = StringUtility.htmlDecode(encoded);
    assertEquals(original, decoded);
  }

}
