/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link JUnitTestClassBrowser}
 */
public class JUnitTestClassBrowserTest {

  @Test
  public void testToRegexPattern_nullAndEmpty() {
    Assert.assertNull(JUnitTestClassBrowser.toRegexPattern(null));
    Assert.assertEquals("", JUnitTestClassBrowser.toRegexPattern(""));
    Assert.assertEquals("", JUnitTestClassBrowser.toRegexPattern("   "));
    Assert.assertEquals("", JUnitTestClassBrowser.toRegexPattern("  \n  \t "));
  }

  @Test
  public void testToRegexPattern_wildcardPatterns() {
    Assert.assertEquals("org", JUnitTestClassBrowser.toRegexPattern("org"));
    Assert.assertEquals("org", JUnitTestClassBrowser.toRegexPattern("org   "));
    Assert.assertEquals("org", JUnitTestClassBrowser.toRegexPattern(" \t org  \n"));
    Assert.assertEquals("org\\.", JUnitTestClassBrowser.toRegexPattern("org."));
    Assert.assertEquals("org\\..*", JUnitTestClassBrowser.toRegexPattern("org.*"));
    Assert.assertEquals("org\\.eclipse\\..*", JUnitTestClassBrowser.toRegexPattern("org.eclipse.*"));
    Assert.assertEquals("org\\.eclipse\\..*\\.testing\\..*", JUnitTestClassBrowser.toRegexPattern("org.eclipse.*.testing.*"));
    Assert.assertEquals("org\\.eclipse\\..*\\.test.ng\\..*", JUnitTestClassBrowser.toRegexPattern("org.eclipse.*.test?ng.*"));
    Assert.assertEquals("org\\.eclipse\\..*\\.test.ng..\\..*", JUnitTestClassBrowser.toRegexPattern("org.eclipse.*.test?ng??.*"));
  }

  @Test
  public void testToRegexPattern_regexPatterns() {
    Assert.assertEquals("org", JUnitTestClassBrowser.toRegexPattern("regex:org"));
    Assert.assertEquals("org", JUnitTestClassBrowser.toRegexPattern("regex:org   "));
    Assert.assertEquals(" \t org", JUnitTestClassBrowser.toRegexPattern("  regex: \t org  \n"));
    Assert.assertEquals("org.", JUnitTestClassBrowser.toRegexPattern("regex:org."));
    Assert.assertEquals("org.*", JUnitTestClassBrowser.toRegexPattern("regex:org.*"));
    Assert.assertEquals("org.eclipse.*", JUnitTestClassBrowser.toRegexPattern("regex:org.eclipse.*"));
    Assert.assertEquals("org.eclipse.*.testing.*", JUnitTestClassBrowser.toRegexPattern("regex:org.eclipse.*.testing.*"));
    Assert.assertEquals("  org\\.[eclips]{7}\\..*\\.testing\\.*", JUnitTestClassBrowser.toRegexPattern("regex:  org\\.[eclips]{7}\\..*\\.testing\\.*"));
  }

  @Test
  public void testParseFilterPatterns_nullAndEmpty() {
    Assert.assertNull(JUnitTestClassBrowser.parseFilterPatterns(null));
    Assert.assertNull(JUnitTestClassBrowser.parseFilterPatterns(""));
    Assert.assertNull(JUnitTestClassBrowser.parseFilterPatterns("    "));
    Assert.assertNull(JUnitTestClassBrowser.parseFilterPatterns(" \n \t "));
    Assert.assertNull(JUnitTestClassBrowser.parseFilterPatterns("  ,   "));
    Assert.assertNull(JUnitTestClassBrowser.parseFilterPatterns("\n,\t"));
  }

  @Test
  public void testParseFilterPatterns_wildcardPatterns() {
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns(", org.eclipse.*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.*  ,   "));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.*, , , "));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.scout.*,org.myproject.*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.scout.*,    org.myproject.*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.scout.*,org.myproject.*    , "));
  }

  @Test
  public void testParseFilterPatterns_regexPatterns() {
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns("regex:org\\.eclipse\\..*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns(", regex:org\\.eclipse\\..*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns("regex:org\\.eclipse\\..*  ,   "));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\..*")}, JUnitTestClassBrowser.parseFilterPatterns("regex:org\\.eclipse\\..*, , , "));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("regex:org\\.eclipse\\.scout\\..*,regex:org\\.myproject\\..*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("regex:org\\.eclipse\\.scout\\..*,    regex:org\\.myproject\\..*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("regex:org\\.eclipse\\.scout\\..*,regex:org\\.myproject\\..*    , "));
  }

  @Test
  public void testParseFilterPatterns_mixedPatterns() {
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.scout.*,regex:org\\.myproject\\..*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.scout.*,regex:org\\.myproject\\..*    "));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("org.eclipse.scout.*,      regex:org\\.myproject\\..*"));
    assertArrayEquals(new Pattern[]{Pattern.compile("org\\.eclipse\\.scout\\..*"), Pattern.compile("org\\.myproject\\..*")},
        JUnitTestClassBrowser.parseFilterPatterns("regex:org\\.eclipse\\.scout\\..*,org.myproject.*"));
  }

  public static void assertArrayEquals(Pattern[] expected, Pattern[] actual) {
    Assert.assertArrayEquals(toStrings(expected), toStrings(actual));
  }

  private static String[] toStrings(Pattern[] patterns) {
    if (patterns == null) {
      return null;
    }
    String[] strings = new String[patterns.length];
    for (int i = 0; i < patterns.length; i++) {
      if (patterns[i] != null) {
        strings[i] = patterns[i].pattern();
      }
    }
    return strings;
  }
}
