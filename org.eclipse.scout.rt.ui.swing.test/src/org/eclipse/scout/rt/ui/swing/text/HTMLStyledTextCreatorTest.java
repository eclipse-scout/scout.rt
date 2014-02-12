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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import org.eclipse.scout.commons.StringUtility;
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
    assertEquals("<!-- -->", StringUtility.getTag(creator.createStyledText(), "td"));
  }

  @Test
  public void testDefaultValues() {
    creator.setText("some text");
    String styledText = creator.createStyledText();
    assertEquals("left", getHorizontalAlignment(styledText));
    assertEquals("top", getVerticalAlignment(styledText));
    assertEquals("", getBackgroundColor(styledText));
    assertEquals("", getForegroundColor(styledText));
    assertEquals(0, getHeight(styledText));
    assertFalse(isTextWrap(styledText));
  }

  @Test
  public void testExistingHtml() {
    creator.setText("some <b> bold </b> text");
    assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    String content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("some <b> bold </b> text", content);

    creator.setText("<html>some <b> bold </b> text</html>");
    assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("some <b> bold </b> text", content);

    creator.setText("<html><body>some <b> bold </b> text</body></html>");
    assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("some <b> bold </b> text", content);

    creator.setText("<html><body>some <b> bold </b> text</body>"); //html end-tag missing
    assertTrue(creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("<html><body>some <b> bold </b> text</body>", content);

    creator.setText("  <html><b>textbold</b><body><i>text italic</i></body></html>  "); //body start-tag missing
    assertTrue(creator.createStyledText().contains("<b>textbold</b><body><i>text italic</i></body>"));
    content = StringUtility.getTag(creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("<b>textbold</b><body><i>text italic</i></body>", content);
  }

  @Test
  public void testLinebreaks() {
    String text = "some text";
    creator.setText(text);
    assertEquals(text, StringUtility.getTag(creator.createStyledText(), "td"));

    text = "line 1 \n line 2 \n\r line 3";
    creator.setText(text);
    assertEquals("line 1 <br> line 2 <br> line 3", StringUtility.getTag(creator.createStyledText(), "td"));

    text = "<html><body>line 1 \n line 2 \n\r line 3</body></html>";
    creator.setText(text);
    assertEquals("line 1 \n line 2 \n\r line 3", StringUtility.getTag(creator.createStyledText(), "td"));

    text = "<html><body>line1 <br> line2 <br /> line3 <br/> line4</body></html>";
    creator.setText(text);
    assertEquals("line1 <br> line2 <br /> line3 <br/> line4", StringUtility.getTag(creator.createStyledText(), "td"));

    text = "line1 <br> line2 <br /> line 3";
    creator.setText(text);
    assertEquals(text, StringUtility.getTag(creator.createStyledText(), "td"));

    text = "line1 <br> line2 <br /> line 3 \n line 4";
    creator.setText(text);
    assertEquals("line1 <br> line2 <br /> line 3 <br> line 4", StringUtility.getTag(creator.createStyledText(), "td"));

    text = "<html> line 1 <br/> line 2 \n line 3</html>";
    creator.setText(text);
    assertEquals("line 1 <br/> line 2 \n line 3", StringUtility.getTag(creator.createStyledText(), "td"));
  }

  @Test
  public void testHorizontalAlignment() {
    creator.setText("some text");

    creator.setHorizontalAlignment(SwingConstants.LEFT);
    assertEquals("left", getHorizontalAlignment(creator.createStyledText()));

    creator.setHorizontalAlignment(SwingConstants.CENTER);
    assertEquals("center", getHorizontalAlignment(creator.createStyledText()));

    creator.setHorizontalAlignment(SwingConstants.RIGHT);
    assertEquals("right", getHorizontalAlignment(creator.createStyledText()));
  }

  @Test
  public void testVerticalAlignment() {
    creator.setText("some text");

    creator.setVerticalAlignment(SwingConstants.TOP);
    assertEquals("top", getVerticalAlignment(creator.createStyledText()));

    creator.setVerticalAlignment(SwingConstants.CENTER);
    assertEquals("middle", getVerticalAlignment(creator.createStyledText()));

    creator.setVerticalAlignment(SwingConstants.BOTTOM);
    assertEquals("bottom", getVerticalAlignment(creator.createStyledText()));
  }

  @Test
  public void testBackgroundColor() {
    creator.setText("some text");

    creator.setBackgroundColor(null);
    assertEquals("", getBackgroundColor(creator.createStyledText()));

    creator.setBackgroundColor(Color.red);
    assertEquals("#ff0000", getBackgroundColor(creator.createStyledText()));
  }

  @Test
  public void testForegroundColor() {
    creator.setText("some text");

    creator.setForegroundColor(null);
    assertEquals("", getForegroundColor(creator.createStyledText()));

    creator.setForegroundColor(Color.red);
    assertEquals("#ff0000", getForegroundColor(creator.createStyledText()));
  }

  @Test
  public void testWrapText() {
    creator.setText("some text");

    creator.setTextWrap(false);
    assertEquals(false, isTextWrap(creator.createStyledText()));

    creator.setTextWrap(true);
    assertEquals(true, isTextWrap(creator.createStyledText()));
  }

  @Test
  public void testHeight() {
    creator.setText("some text");
    creator.setHeight(123);
    assertEquals(123, getHeight(creator.createStyledText()));
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
