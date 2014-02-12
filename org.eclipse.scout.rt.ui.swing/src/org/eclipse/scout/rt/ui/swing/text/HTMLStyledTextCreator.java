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
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;

/**
 * Creates a styledText using HTML tags and attributes.
 * <ul>
 * <li>horizontal alignment: a <code>div</code> is used
 * <li>vertical alignment: a <code>table</code> is used
 * <li>background color: the color is assigned to the <code>body</code> tag
 * <li>foreground color: the color is assigned to the <code>body</code> tag
 * <li>text wrapping: the <code>td</code> tag's 'white-space' attribute is used
 * </ul>
 * 
 * @since 3.10.0-M5
 */
public class HTMLStyledTextCreator implements IStyledTextCreator {

  private static final Pattern HTML_PATTERN = Pattern.compile("<html>(.*)</html>", Pattern.DOTALL);
  private static final Pattern BODY_PATTERN = Pattern.compile("<body[^>]*>(.*)</body>", Pattern.DOTALL);

  private Color m_backgroundColor;
  private Color m_foregroundColor;
  private int m_horizontalAlignment = Integer.MIN_VALUE;
  private int m_verticalAlignment = Integer.MIN_VALUE;
  private int m_height;
  private String m_text;
  private boolean m_textWrap;

  @Override
  public IStyledTextCreator setText(String text) {
    this.m_text = text;
    return this;
  }

  public String getText() {
    return this.m_text;
  }

  @Override
  public IStyledTextCreator setBackgroundColor(Color color) {
    this.m_backgroundColor = color;
    return this;
  }

  public Color getBackgroundColor() {
    return this.m_backgroundColor;
  }

  @Override
  public IStyledTextCreator setForegroundColor(Color color) {
    this.m_foregroundColor = color;
    return this;
  }

  public Color getForegroundColor() {
    return this.m_foregroundColor;
  }

  @Override
  public IStyledTextCreator setHorizontalAlignment(int scoutAlign) {
    this.m_horizontalAlignment = scoutAlign;
    return this;
  }

  public int getHorizontalAlignment() {
    return this.m_horizontalAlignment;
  }

  @Override
  public IStyledTextCreator setVerticalAlignment(int scoutAlign) {
    this.m_verticalAlignment = scoutAlign;
    return this;
  }

  public int getVerticalAlignment() {
    return this.m_verticalAlignment;
  }

  @Override
  public IStyledTextCreator setHeight(int height) {
    this.m_height = height;
    return this;
  }

  public int getHeight() {
    return this.m_height;
  }

  @Override
  public IStyledTextCreator setTextWrap(boolean wrap) {
    this.m_textWrap = wrap;
    return this;
  }

  public boolean isTextWrap() {
    return this.m_textWrap;
  }

  @Override
  public String createStyledText() {
    if (!StringUtility.hasText(getText())) {
      setText("<!-- -->"); //This is necessary, otherwise the HTMLEditorKit will remove our tags if String is empty
    }

    String styledText = getText().trim();

    //remove html and body tags since we will add our owns
    boolean originalTextIsHTML = false;
    if (HTML_PATTERN.matcher(styledText).matches()) {
      styledText = StringUtility.getTag(styledText, "html");
      originalTextIsHTML = true;
    }

    if (BODY_PATTERN.matcher(styledText).matches()) {
      styledText = StringUtility.getTag(styledText, "body");
    }

    if (!originalTextIsHTML) {
      styledText = StringUtility.replaceNewLines(styledText, "<br>");
    }

    styledText = "<html><body style=\"" + getBodyStyle() + "\"><div align=\"" + getAlign() + "\"><table cellpadding=\"0\" cellspacing=\"0\"><tr><td valign=\"" + getValign() + "\" height=\"" + getHeight() + "px\" style=\"white-space:" + getWrapText() + ";\">" + styledText + "</td></tr></table></div></body></html>";
    return styledText;
  }

  protected String getValign() {
    if (getVerticalAlignment() == SwingConstants.CENTER) {
      return "middle";
    }
    else if (getVerticalAlignment() == SwingConstants.BOTTOM) {
      return "bottom";
    }
    return "top";
  }

  protected String getAlign() {
    if (getHorizontalAlignment() == SwingConstants.HORIZONTAL) {
      return "center";
    }
    else if (getHorizontalAlignment() == SwingConstants.RIGHT) {
      return "right";
    }
    return "left";
  }

  protected String getWrapText() {
    if (!isTextWrap()) {
      return "nowrap";
    }
    return "normal";
  }

  protected String getBodyStyle() {
    String bodyStyle = "";
    if (StringUtility.hasText(ColorUtility.createStringFromColor(getBackgroundColor()))) {
      bodyStyle += "background-color: " + ColorUtility.createStringFromColor(getBackgroundColor()) + ";";
    }

    if (StringUtility.hasText(ColorUtility.createStringFromColor(getForegroundColor()))) {
      bodyStyle += "color: " + ColorUtility.createStringFromColor(getForegroundColor()) + ";";
    }
    return bodyStyle;
  }
}
