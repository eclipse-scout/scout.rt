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

  private static final Pattern HTML_PATTERN = Pattern.compile("<html>(.*)</html>");
  private static final Pattern BODY_PATTERN = Pattern.compile("<body[^>]*>(.*)</body>");

  private Color m_backgroundColor;
  private Color m_foregroundColor;
  private int m_horizontalAlignment = Integer.MIN_VALUE;
  private int m_verticalAlignment = Integer.MIN_VALUE;
  private int m_height;
  private String m_text;
  private boolean m_textWrap;

  @Override
  public void setText(String text) {
    this.m_text = text;
  }

  public String getText() {
    return this.m_text;
  }

  @Override
  public void setBackgroundColor(Color color) {
    this.m_backgroundColor = color;
  }

  public Color getBackgroundColor() {
    return this.m_backgroundColor;
  }

  @Override
  public void setForegroundColor(Color color) {
    this.m_foregroundColor = color;
  }

  public Color getForegroundColor() {
    return this.m_foregroundColor;
  }

  @Override
  public void setHorizontalAlignment(int scoutAlign) {
    this.m_horizontalAlignment = scoutAlign;
  }

  public int getHorizontalAlignment() {
    return this.m_horizontalAlignment;
  }

  @Override
  public void setVerticalAlignment(int scoutAlign) {
    this.m_verticalAlignment = scoutAlign;
  }

  public int getVerticalAlignment() {
    return this.m_verticalAlignment;
  }

  @Override
  public void setHeight(int height) {
    this.m_height = height;
  }

  public int getHeight() {
    return this.m_height;
  }

  @Override
  public void setTextWrap(boolean wrap) {
    this.m_textWrap = wrap;
  }

  public boolean isTextWrap() {
    return this.m_textWrap;
  }

  @Override
  public String createStyledText() {
    if (!StringUtility.hasText(getText())) {
      setText("<!-- -->"); //This is necessary, otherwise the HTMLEditorKit will remove our tags if String is empty
    }

    String styledText = getText();

    //remove html and body tags since we will add our owns
    if (HTML_PATTERN.matcher(styledText).matches()) {
      styledText = StringUtility.getTag(styledText, "html");
    }

    if (BODY_PATTERN.matcher(styledText).matches()) {
      styledText = StringUtility.getTag(styledText, "body");
    }

    String align = "left";
    String valign = "top";

    if (getHorizontalAlignment() == SwingConstants.HORIZONTAL) {
      align = "center";
    }
    else if (getHorizontalAlignment() == SwingConstants.RIGHT) {
      align = "right";
    }
    else {
      align = "left";
    }

    if (getVerticalAlignment() == SwingConstants.CENTER) {
      valign = "middle";
    }
    else if (getVerticalAlignment() == SwingConstants.BOTTOM) {
      valign = "bottom";
    }
    else {
      valign = "top";
    }

    String wrapText = "normal";
    if (!isTextWrap()) {
      wrapText = "nowrap";
    }

    String bodyStyle = "";
    if (StringUtility.hasText(ColorUtility.createStringFromColor(getBackgroundColor()))) {
      bodyStyle += "background-color: " + ColorUtility.createStringFromColor(getBackgroundColor()) + ";";
    }

    if (StringUtility.hasText(ColorUtility.createStringFromColor(getForegroundColor()))) {
      bodyStyle += "color: " + ColorUtility.createStringFromColor(getForegroundColor()) + ";";
    }

    styledText = "<html><body style=\"" + bodyStyle + "\"><div align=\"" + align + "\"><table cellpadding=\"0\" cellspacing=\"0\"><tr><td valign=\"" + valign + "\" height=\"" + getHeight() + "px\" style=\"white-space:" + wrapText + ";\">" + styledText + "</td></tr></table></div></body></html>";
    return styledText;
  }

}
