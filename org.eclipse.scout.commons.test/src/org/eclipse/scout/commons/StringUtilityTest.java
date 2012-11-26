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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class StringUtilityTest extends Assert {

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

  /**
   * ticket 88592
   */
  @Test
  public void testMnemonics() {
    String s = "Button &Test";
    Assert.assertEquals('T', StringUtility.getMnemonic(s));
    Assert.assertEquals("Button Test", StringUtility.removeMnemonic(s));
    s = "Button & Test";
    Assert.assertEquals(0x00, StringUtility.getMnemonic(s));
    Assert.assertEquals(s, StringUtility.removeMnemonic(s));
    s = "&test";
    Assert.assertEquals('t', StringUtility.getMnemonic(s));
    Assert.assertEquals("test", StringUtility.removeMnemonic(s));
    s = "test &";
    Assert.assertEquals(0x00, StringUtility.getMnemonic(s));
    Assert.assertEquals(s, StringUtility.removeMnemonic(s));
  }

  // UTF-8 length is 13 to avoid accidental buffer size matches
  static final String CHARACTERS = "aouäöüàé";

  /**
   * ticket 90988
   */
  @Test
  public void testDecompress_umlauts() throws Exception {

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 100000; i++) {
      builder.append(CHARACTERS);
    }

    String original = builder.toString();
    String decompressed = StringUtility.decompress(StringUtility.compress(original));

    Assert.assertEquals(original, decompressed);
  }

  @Test
  public void testTags() throws Exception {
    String s;
    s = "foo <a>text</a> bar";
    Assert.assertEquals("foo  bar", StringUtility.removeTag(s, "a"));
    s = "foo <a>text</a> <a>text</a> bar";
    Assert.assertEquals("foo   bar", StringUtility.removeTag(s, "a"));
    s = "foo <a>text</a> <a>bar";
    Assert.assertEquals("foo  <a>bar", StringUtility.removeTag(s, "a"));
    s = "foo <a>text</a> </a>bar";
    Assert.assertEquals("foo  </a>bar", StringUtility.removeTag(s, "a"));
    s = "foo <a/> bar";
    Assert.assertEquals("foo  bar", StringUtility.removeTag(s, "a"));
  }

  @Test
  public void testRegExPattern() throws Exception {
    String s;
    s = "test*";
    Assert.assertEquals("test.*", StringUtility.toRegExPattern(s));

    s = "test?";
    Assert.assertEquals("test.", StringUtility.toRegExPattern(s));

    s = "com.test.*";
    Assert.assertEquals("com\\.test\\..*", StringUtility.toRegExPattern(s));
  }
}
