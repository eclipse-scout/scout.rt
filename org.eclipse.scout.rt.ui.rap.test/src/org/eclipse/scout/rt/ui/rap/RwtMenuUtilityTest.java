package org.eclipse.scout.rt.ui.rap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link RwtMenuUtility}
 *
 * @since 5.0-M2
 */
public class RwtMenuUtilityTest {

  @Test
  public void testFormatKeystroke() {
    assertEquals("", RwtMenuUtility.formatKeystroke(""));
    assertEquals("", RwtMenuUtility.formatKeystroke(null));
    assertEquals("a", RwtMenuUtility.formatKeystroke("a"));
    assertEquals("F11", RwtMenuUtility.formatKeystroke("F11"));
    assertEquals("F11", RwtMenuUtility.formatKeystroke("f11"));
    assertEquals("", RwtMenuUtility.formatKeystroke("undefinedkeystroke"));
  }
}
