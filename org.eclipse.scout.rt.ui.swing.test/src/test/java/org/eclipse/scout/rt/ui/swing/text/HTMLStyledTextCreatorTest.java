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

  IStyledTextCreator m_creator;

  @Before
  public void createStyledText() {
    m_creator = new HTMLStyledTextCreator();
  }

  @Test
  public void testNull() {
    m_creator.setText(null);
    assertEquals("<!-- -->", StringUtility.getTag(m_creator.createStyledText(), "td"));
  }

  @Test
  public void testDefaultValues() {
    m_creator.setText("some text");
    String styledText = m_creator.createStyledText();
    assertEquals("left", getHorizontalAlignment(styledText));
    assertEquals("top", getVerticalAlignment(styledText));
    assertEquals("", getBackgroundColor(styledText));
    assertEquals("", getForegroundColor(styledText));
    assertEquals(0, getHeight(styledText));
    assertFalse(isTextWrap(styledText));
  }

  @Test
  public void testExistingHtml() {
    m_creator.setText("some <b> bold </b> text");
    assertTrue(m_creator.createStyledText().contains("some <b> bold </b> text"));
    String content = StringUtility.getTag(m_creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("some <b> bold </b> text", content);

    m_creator.setText("<html>some <b> bold </b> text</html>");
    assertTrue(m_creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(m_creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("some <b> bold </b> text", content);

    m_creator.setText("<html><body>some <b> bold </b> text</body></html>");
    assertTrue(m_creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(m_creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("some <b> bold </b> text", content);

    m_creator.setText("<html><body>some <b> bold </b> text</body>"); //html end-tag missing
    assertTrue(m_creator.createStyledText().contains("some <b> bold </b> text"));
    content = StringUtility.getTag(m_creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("<html><body>some <b> bold </b> text</body>", content);

    m_creator.setText("  <html><b>textbold</b><body><i>text italic</i></body></html>  "); //body start-tag missing
    assertTrue(m_creator.createStyledText().contains("<b>textbold</b><body><i>text italic</i></body>"));
    content = StringUtility.getTag(m_creator.createStyledText(), "td"); //content is inside the table cell
    assertEquals("<b>textbold</b><body><i>text italic</i></body>", content);
  }

  @Test
  public void testLinebreaks() {
    String text = "some text";
    m_creator.setText(text);
    assertEquals(text, StringUtility.getTag(m_creator.createStyledText(), "td"));

    text = "line 1 \n line 2 \n\r line 3";
    m_creator.setText(text);
    assertEquals("line 1 <br> line 2 <br> line 3", StringUtility.getTag(m_creator.createStyledText(), "td"));

    text = "<html><body>line 1 \n line 2 \n\r line 3</body></html>";
    m_creator.setText(text);
    assertEquals("line 1 \n line 2 \n\r line 3", StringUtility.getTag(m_creator.createStyledText(), "td"));

    text = "<html><body>line1 <br> line2 <br /> line3 <br/> line4</body></html>";
    m_creator.setText(text);
    assertEquals("line1 <br> line2 <br /> line3 <br/> line4", StringUtility.getTag(m_creator.createStyledText(), "td"));

    text = "line1 <br> line2 <br /> line 3";
    m_creator.setText(text);
    assertEquals(text, StringUtility.getTag(m_creator.createStyledText(), "td"));

    text = "line1 <br> line2 <br /> line 3 \n line 4";
    m_creator.setText(text);
    assertEquals("line1 <br> line2 <br /> line 3 <br> line 4", StringUtility.getTag(m_creator.createStyledText(), "td"));

    text = "<html> line 1 <br/> line 2 \n line 3</html>";
    m_creator.setText(text);
    assertEquals("line 1 <br/> line 2 \n line 3", StringUtility.getTag(m_creator.createStyledText(), "td"));
  }

  @Test
  public void testHorizontalAlignment() {
    m_creator.setText("some text");

    m_creator.setHorizontalAlignment(SwingConstants.LEFT);
    assertEquals("left", getHorizontalAlignment(m_creator.createStyledText()));

    m_creator.setHorizontalAlignment(SwingConstants.CENTER);
    assertEquals("center", getHorizontalAlignment(m_creator.createStyledText()));

    m_creator.setHorizontalAlignment(SwingConstants.RIGHT);
    assertEquals("right", getHorizontalAlignment(m_creator.createStyledText()));
  }

  @Test
  public void testVerticalAlignment() {
    m_creator.setText("some text");

    m_creator.setVerticalAlignment(SwingConstants.TOP);
    assertEquals("top", getVerticalAlignment(m_creator.createStyledText()));

    m_creator.setVerticalAlignment(SwingConstants.CENTER);
    assertEquals("middle", getVerticalAlignment(m_creator.createStyledText()));

    m_creator.setVerticalAlignment(SwingConstants.BOTTOM);
    assertEquals("bottom", getVerticalAlignment(m_creator.createStyledText()));
  }

  @Test
  public void testBackgroundColor() {
    m_creator.setText("some text");

    m_creator.setBackgroundColor(null);
    assertEquals("", getBackgroundColor(m_creator.createStyledText()));

    m_creator.setBackgroundColor(Color.red);
    assertEquals("#ff0000", getBackgroundColor(m_creator.createStyledText()));
  }

  @Test
  public void testForegroundColor() {
    m_creator.setText("some text");

    m_creator.setForegroundColor(null);
    assertEquals("", getForegroundColor(m_creator.createStyledText()));

    m_creator.setForegroundColor(Color.red);
    assertEquals("#ff0000", getForegroundColor(m_creator.createStyledText()));
  }

  @Test
  public void testWrapText() {
    m_creator.setText("some text");

    m_creator.setTextWrap(false);
    assertEquals(false, isTextWrap(m_creator.createStyledText()));

    m_creator.setTextWrap(true);
    assertEquals(true, isTextWrap(m_creator.createStyledText()));
  }

  @Test
  public void testHeight() {
    m_creator.setText("some text");
    m_creator.setHeight(123);
    assertEquals(123, getHeight(m_creator.createStyledText()));
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
      return Integer.parseInt(m.group(1));
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
