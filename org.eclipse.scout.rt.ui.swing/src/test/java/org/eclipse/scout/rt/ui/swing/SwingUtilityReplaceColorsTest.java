package org.eclipse.scout.rt.ui.swing;

/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for {@link SwingUtility#replace3DigitColors(String)}
 */
public class SwingUtilityReplaceColorsTest {

  private static final String EMPTY_HTML = "<html><head></head><body></body></html>";

  private static final String DIV_3_DIGIT_COLOR =
      "<div style=\"background-color:#def\"  style=\'background-color:#000\'>" +
          "    Dies ist ein Test<br>" +
          " </div>";
  private static final String DIV_3_DIGIT_COLOR_2 =
      "<div style=\"background-color:#abc\">" +
          "    Dies ist ein Test<br>" +
          " </div>";

  private static final String DIV_6_DIGIT_COLOR =
      "<div style=\"background-color:#ddeeff\"  style=\'background-color:#000000\'>" +
          "    Dies ist ein Test<br>" +
          " </div>";

  private static final String DIV_6_DIGIT_COLOR_2 =
      "<div style=\"background-color:#aabbcc\">" +
          "    Dies ist ein Test<br>" +
          " </div>";

  private static final String HTML_3_DIGIT_COLOR = "" +
      "<html>" +
      "<head>" +
      "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
      "</head>" +
      "<body style=\"overflow:auto;\">" +
      DIV_3_DIGIT_COLOR +
      "</body>" +
      "</html>";

  private static final String HTML_6_DIGIT_COLOR = "" +
      "<html>" +
      "<head>" +
      "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
      "</head>" +
      "<body style=\"overflow:auto;\">" +
      DIV_6_DIGIT_COLOR +
      "</body>" +
      "</html>";

  private static final String HTML_6_DIGIT_COLOR_2 = "" +
      "<html>" +
      "<head>" +
      "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
      "</head>" +
      "<body style=\"overflow:auto;\">" +
      DIV_6_DIGIT_COLOR +
      "Test" +
      DIV_6_DIGIT_COLOR_2 +
      "</body>" +
      "</html>";
  private static final String HTML_3_DIGIT_COLOR_2 = "" +
      "<html>" +
      "<head>" +
      "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
      "</head>" +
      "<body style=\"overflow:auto;\">" +
      DIV_3_DIGIT_COLOR +
      "Test" +
      DIV_3_DIGIT_COLOR_2 +
      "</body>" +
      "</html>";

  @Test
  public void testReplace3DigitColorsDivs() {
    assertEquals(DIV_6_DIGIT_COLOR, SwingUtility.replace3DigitColors(DIV_3_DIGIT_COLOR));
    assertEquals(DIV_6_DIGIT_COLOR, SwingUtility.replace3DigitColors(DIV_6_DIGIT_COLOR));
  }

  @Test
  public void testReplace3DigitColors() {
    assertEquals(DIV_6_DIGIT_COLOR, SwingUtility.replace3DigitColors(DIV_3_DIGIT_COLOR));
    assertEquals(HTML_6_DIGIT_COLOR, SwingUtility.replace3DigitColors(HTML_3_DIGIT_COLOR));
    assertEquals(HTML_6_DIGIT_COLOR_2, SwingUtility.replace3DigitColors(HTML_3_DIGIT_COLOR_2));
  }

  @Test
  public void test3DigitsNoReplacement() {
    assertEquals(EMPTY_HTML, SwingUtility.replace3DigitColors(EMPTY_HTML));
    assertEquals(HTML_6_DIGIT_COLOR_2, SwingUtility.replace3DigitColors(HTML_6_DIGIT_COLOR_2));
  }

  @Test
  public void test3DigitsNullHtml() throws Exception {
    assertNull(SwingUtility.replace3DigitColors(null));
  }

}
