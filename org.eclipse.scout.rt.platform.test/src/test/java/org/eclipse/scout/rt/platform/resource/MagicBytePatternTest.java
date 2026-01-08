/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Assert;
import org.junit.Test;

public class MagicBytePatternTest {

  @Test
  public void testBytePatternCreation() {
    runTestBytePatternCreation("aabb", new byte[]{-86, -69}, new boolean[]{false, false});
    runTestBytePatternCreation("aaxxbb", new byte[]{-86, 0, -69}, new boolean[]{false, true, false});
    runTestBytePatternCreation("xxbb", new byte[]{0, -69}, new boolean[]{true, false});
    runTestBytePatternCreation("aaxx", new byte[]{-86, 0}, new boolean[]{false, true});
    runTestBytePatternCreation("aaxxbbxxcc", new byte[]{-86, 0, -69, 0, -52}, new boolean[]{false, true, false, true, false});
    runTestBytePatternCreation("aaxxxxxxbb", new byte[]{-86, 0, 0, 0, -69}, new boolean[]{false, true, true, true, false});
    runTestBytePatternCreation("aaaaaaaaxxxxxxbbbbbb", new byte[]{-86, -86, -86, -86, 0, 0, 0, -69, -69, -69}, new boolean[]{false, false, false, false, true, true, true, false, false, false});
    runTestBytePatternCreation("aaaa aaaa xxxx xxbb bbbb", new byte[]{-86, -86, -86, -86, 0, 0, 0, -69, -69, -69}, new boolean[]{false, false, false, false, true, true, true, false, false, false});
    runTestBytePatternCreation("AAAA AAAA XXXX XXBB BBBB", new byte[]{-86, -86, -86, -86, 0, 0, 0, -69, -69, -69}, new boolean[]{false, false, false, false, true, true, true, false, false, false});

    assertBytePatternThrows("", AssertionException.class, "Assertion error: hexMagic must not be empty");
    assertBytePatternThrows("a", AssertionException.class, "Assertion error: hexMagic must contain an even number of chars");
    assertBytePatternThrows("ax", AssertionException.class, "Assertion error: Expect finding skip char at even char indexes");
    assertBytePatternThrows("aaxb", AssertionException.class, "Assertion error: Skip chars must come in pairs");
  }

  protected void runTestBytePatternCreation(String hexMagic, byte[] expectedBytes, boolean[] expectedSkips) {
    MagicBytePattern bytePattern = new MagicBytePattern(0, hexMagic);
    assertThat(bytePattern.m_bytes, is(expectedBytes));
    assertThat(bytePattern.m_skips, is(expectedSkips));
  }

  protected void assertBytePatternThrows(String hexMagic, Class<? extends Exception> expectedException, String expectedMessage) {
    Exception actualException = Assert.assertThrows(expectedException, () -> new MagicBytePattern(0, hexMagic));

    assertThat(actualException.getMessage(), is(expectedMessage));
  }

  @Test
  public void testBytePatternMatches() {
    runTestBytePatternMatches(0, "aa", new byte[]{-86}, true);
    runTestBytePatternMatches(0, "aa", new byte[]{33}, false);
    runTestBytePatternMatches(0, "aabb", new byte[]{-86, -69}, true);
    runTestBytePatternMatches(0, "aabb", new byte[]{11, 22}, false);
    runTestBytePatternMatches(0, "aaxxbb", new byte[]{-86, 33, -69}, true);
    runTestBytePatternMatches(4, "aaxxbb", new byte[]{-128, -127, -126, -125, -86, 33, -69}, true);
    runTestBytePatternMatches(0, "aaxxbb", new byte[]{-128, -127, -126, -125, -86, 33, -69}, false);
  }

  protected void runTestBytePatternMatches(int pos, String hexMagic, byte[] content, boolean expectedMatches) {
    MagicBytePattern bytePattern = new MagicBytePattern(pos, hexMagic);
    assertThat(bytePattern.matches(content), is(expectedMatches));
  }
}
