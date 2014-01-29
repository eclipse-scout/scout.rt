/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.text;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import org.eclipse.scout.commons.StringUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link HTMLStyledTextCreator}
 */
public class HTMLStyledTextCreatorTest {

  IStyledTextCreator creator;

  @Before
  public void createStyledText() {
    creator = new HTMLStyledTextCreator();
  }

  @Test
  public void testNull() {
    creator.setText(null);
    Assert.assertEquals("<!-- -->", StringUtility.getTag(creator.createStyledText(), "td"));
  }

  @Test
  public void testDefaultValues() {
    creator.setText("some text");
    String styledText = creator.createStyledText();
    Assert.assertEquals("left", getHorizontalAlignment(styledText));
    Assert.assertEquals("top", getVerticalAlignment(styledText));
    Assert.assertEquals("", getBackgroundColor(styledText));
    Assert.assertEquals("", getForegroundColor(styledText));
    Assert.assertEquals(0, getHeight(styledText));
    Assert.assertFalse(isTextWrap(styledText));
  }

  @Test
  public void testExistingHtml() {
    creator.setText("some <b> bold </b> text");
    Assert.assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    String content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    Assert.assertEquals("some <b> bold </b> text", content);

    creator.setText("<html>some <b> bold </b> text</html>");
    Assert.assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    Assert.assertEquals("some <b> bold </b> text", content);

    creator.setText("<html><body>some <b> bold </b> text</body></html>");
    Assert.assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    Assert.assertEquals("some <b> bold </b> text", content);

    creator.setText("<html><body>some <b> bold </b> text</body>"); //html end-tag missing
    Assert.assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    Assert.assertEquals("<html><body>some <b> bold </b> text</body>", content);
  }

  @Test
  public void testHorizontalAlignment() {
    creator.setText("some text");

    creator.setHorizontalAlignment(SwingConstants.LEFT);
    Assert.assertEquals("left", getHorizontalAlignment(creator.createStyledText()));

    creator.setHorizontalAlignment(SwingConstants.CENTER);
    Assert.assertEquals("center", getHorizontalAlignment(creator.createStyledText()));

    creator.setHorizontalAlignment(SwingConstants.RIGHT);
    Assert.assertEquals("right", getHorizontalAlignment(creator.createStyledText()));
  }

  @Test
  public void testVerticalAlignment() {
    creator.setText("some text");

    creator.setVerticalAlignment(SwingConstants.TOP);
    Assert.assertEquals("top", getVerticalAlignment(creator.createStyledText()));

    creator.setVerticalAlignment(SwingConstants.CENTER);
    Assert.assertEquals("middle", getVerticalAlignment(creator.createStyledText()));

    creator.setVerticalAlignment(SwingConstants.BOTTOM);
    Assert.assertEquals("bottom", getVerticalAlignment(creator.createStyledText()));
  }

  @Test
  public void testBackgroundColor() {
    creator.setText("some text");

    creator.setBackgroundColor(null);
    Assert.assertEquals("", getBackgroundColor(creator.createStyledText()));

    creator.setBackgroundColor(Color.red);
    Assert.assertEquals("#ff0000", getBackgroundColor(creator.createStyledText()));
  }

  @Test
  public void testForegroundColor() {
    creator.setText("some text");

    creator.setForegroundColor(null);
    Assert.assertEquals("", getForegroundColor(creator.createStyledText()));

    creator.setForegroundColor(Color.red);
    Assert.assertEquals("#ff0000", getForegroundColor(creator.createStyledText()));
  }

  @Test
  public void testWrapText() {
    creator.setText("some text");

    creator.setTextWrap(false);
    Assert.assertEquals(false, isTextWrap(creator.createStyledText()));

    creator.setTextWrap(true);
    Assert.assertEquals(true, isTextWrap(creator.createStyledText()));
  }

  @Test
  public void testHeight() {
    creator.setText("some text");

    creator.setHeight(123);
    Assert.assertEquals(123, getHeight(creator.createStyledText()));
  }

  private String getHorizontalAlignment(String styledText) {
    if (styledText.contains("<div align=\"left\">")) {
      return "left";
    }
    else if (styledText.contains("<div align=\"center\">")) {
      return "center";
    }
    else if (styledText.contains("<div align=\"right\">")) {
      return "right";
    }
    else {
      return "";
    }
  }

  private String getVerticalAlignment(String styledText) {
    if (styledText.contains("valign=\"top\"")) {
      return "top";
    }
    else if (styledText.contains("valign=\"middle\"")) {
      return "middle";
    }
    else if (styledText.contains("valign=\"bottom\"")) {
      return "bottom";
    }
    else {
      return "";
    }
  }

  private int getHeight(String styledText) {
    Pattern p = Pattern.compile(".*height=\\s*\"(\\d+)\\s*px\".*");
    Matcher m = p.matcher(styledText);
    if (m.matches()) {
      return Integer.valueOf(m.group(1));
    }
    return 0;
  }

  private String getBackgroundColor(String styledText) {
    Pattern p = Pattern.compile(".*background-color:\\s*(#[0-f0-F]*);.*");
    Matcher m = p.matcher(styledText);
    if (m.matches()) {
      return m.group(1);
    }
    return "";
  }

  private String getForegroundColor(String styledText) {
    Pattern p = Pattern.compile(".*[^-]color:\\s*(#[0-f0-F]*);.*");
    Matcher m = p.matcher(styledText);
    if (m.matches()) {
      return m.group(1);
    }
    return "";
  }

  private boolean isTextWrap(String styledText) {
    Pattern p = Pattern.compile(".*white-space:\\s*normal.*");
    Matcher m = p.matcher(styledText);
    if (m.matches()) {
      return true;
    }
    return false;
  }
}
