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
package org.eclipse.scout.rt.client.ui.action.keystroke;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link KeyStrokeNormalizer}
 *
 * @since 5.0-M2
 */
public class KeyStrokeNormalizerTest {

  @Test
  public void testValidKeystrokes() {
    KeyStrokeNormalizer keystroke = new KeyStrokeNormalizer("a");
    assertKeystrokeValid(keystroke, "a", "a", false, false, false);

    keystroke = new KeyStrokeNormalizer("A");
    assertKeystrokeValid(keystroke, "a", "a", false, false, false);

    keystroke = new KeyStrokeNormalizer("A ");
    assertKeystrokeValid(keystroke, "a", "a", false, false, false);

    keystroke = new KeyStrokeNormalizer(" A");
    assertKeystrokeValid(keystroke, "a", "a", false, false, false);

    keystroke = new KeyStrokeNormalizer("shift-a");
    assertKeystrokeValid(keystroke, "shift-a", "a", false, false, true);

    keystroke = new KeyStrokeNormalizer("ShIfT-a");
    assertKeystrokeValid(keystroke, "shift-a", "a", false, false, true);

    keystroke = new KeyStrokeNormalizer("control-A");
    assertKeystrokeValid(keystroke, "control-a", "a", false, true, false);

    keystroke = new KeyStrokeNormalizer("CoNtROL-A");
    assertKeystrokeValid(keystroke, "control-a", "a", false, true, false);

    keystroke = new KeyStrokeNormalizer("ctrl-A");
    assertKeystrokeValid(keystroke, "control-a", "a", false, true, false);

    keystroke = new KeyStrokeNormalizer("contrOl-A");
    assertKeystrokeValid(keystroke, "control-a", "a", false, true, false);

    keystroke = new KeyStrokeNormalizer("strg-A");
    assertKeystrokeValid(keystroke, "control-a", "a", false, true, false);

    keystroke = new KeyStrokeNormalizer("STRG-F12");
    assertKeystrokeValid(keystroke, "control-f12", "f12", false, true, false);

    keystroke = new KeyStrokeNormalizer("alternate-1");
    assertKeystrokeValid(keystroke, "alternate-1", "1", true, false, false);

    keystroke = new KeyStrokeNormalizer("alternaTE-1");
    assertKeystrokeValid(keystroke, "alternate-1", "1", true, false, false);

    keystroke = new KeyStrokeNormalizer("alt-1");
    assertKeystrokeValid(keystroke, "alternate-1", "1", true, false, false);

    keystroke = new KeyStrokeNormalizer("ALT-1");
    assertKeystrokeValid(keystroke, "alternate-1", "1", true, false, false);

    keystroke = new KeyStrokeNormalizer("insert");
    assertKeystrokeValid(keystroke, "insert", "insert", false, false, false);

    keystroke = new KeyStrokeNormalizer("");
    assertKeystrokeValid(keystroke, null, null, false, false, false);

    keystroke = new KeyStrokeNormalizer(null);
    assertKeystrokeValid(keystroke, null, null, false, false, false);

    keystroke = new KeyStrokeNormalizer("ALT-shIft-f4");
    assertKeystrokeValid(keystroke, "shift-alternate-f4", "f4", true, false, true);

    keystroke = new KeyStrokeNormalizer("ALT-Ctrl-shIft-f4");
    assertKeystrokeValid(keystroke, "shift-control-alternate-f4", "f4", true, true, true);

    keystroke = new KeyStrokeNormalizer("-");
    assertKeystrokeValid(keystroke, "-", "-", false, false, false);

    keystroke = new KeyStrokeNormalizer("ctrl--");
    assertKeystrokeValid(keystroke, "control--", "-", false, true, false);
  }

  @Test
  public void testInvalidKeystrokes() {
    KeyStrokeNormalizer keystroke = new KeyStrokeNormalizer("alt-shiift-a");
    assertKeystrokeInvalid(keystroke);

    keystroke = new KeyStrokeNormalizer("aalt-shift-a");
    assertKeystrokeInvalid(keystroke);

    keystroke = new KeyStrokeNormalizer("f1-F2");
    assertKeystrokeInvalid(keystroke);

    keystroke = new KeyStrokeNormalizer("alt-shiift-");
    assertKeystrokeInvalid(keystroke);

    keystroke = new KeyStrokeNormalizer("alt");
    assertKeystrokeInvalid(keystroke);
  }

  /**
   * @param ksn
   * @param normalizedKeystroke
   * @param key
   * @param isValid
   * @param hasAlt
   * @param hasCtrl
   * @param hasShift
   */
  private void assertKeystroke(KeyStrokeNormalizer ksn, String normalizedKeystroke, String key, boolean isValid, boolean hasAlt, boolean hasCtrl, boolean hasShift) {
    ksn.normalize();
    assertEquals("normalizedKeystroke should be " + normalizedKeystroke, normalizedKeystroke, ksn.getNormalizedKeystroke());
    assertEquals("key should be " + key, key, ksn.getKey());
    assertEquals("keyStroke should be valid = " + isValid, isValid, ksn.isValid());
    assertEquals("keyStroke should have ALT = " + hasAlt, hasAlt, ksn.hasAlt());
    assertEquals("keyStroke should have CTRL = " + hasCtrl, hasCtrl, ksn.hasCtrl());
    assertEquals("keyStroke should have SHIFT = " + hasShift, hasShift, ksn.hasShift());
  }

  /**
   * @param ksn
   * @param normalizedKeystroke
   * @param key
   * @param hasAlt
   * @param hasCtrl
   * @param hasShift
   */
  private void assertKeystrokeValid(KeyStrokeNormalizer ksn, String normalizedKeystroke, String key, boolean hasAlt, boolean hasCtrl, boolean hasShift) {
    assertKeystroke(ksn, normalizedKeystroke, key, true, hasAlt, hasCtrl, hasShift);
  }

  /**
   * @param ksn
   */
  private void assertKeystrokeInvalid(KeyStrokeNormalizer ksn) {
    assertKeystroke(ksn, null, null, false, false, false, false);
  }
}
