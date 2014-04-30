/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields.groupbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;

import org.eclipse.scout.commons.StringUtility;

public class TitledGroupBorder implements Border {
  private static final int SPACE_BEFORE = 12;
  private static final int H_GAP_BEFORE = 3, H_GAP_AFTER = 1;
  private static final int V_GAP = 1;
  private String m_text;
  private Insets m_minInsets;

  public TitledGroupBorder(String text, Insets minInsets) {
    m_text = text;
    m_minInsets = minInsets;
  }

  public String getText() {
    return m_text;
  }

  private Border getBorder() {
    return UIManager.getBorder("TitledBorder.border");
  }

  public Color getTextColor(Component c) {
    Color col;
    if (c.isEnabled()) {
      col = c.getForeground();
    }
    else {
      col = UIManager.getColor("textInactiveText");
    }
    if (col == null) {
      col = UIManager.getColor("TitledBorder.titleColor");
    }
    if (col == null) {
      col = Color.darkGray;
    }
    return col;
  }

  public Font getTextFont() {
    Font f = UIManager.getFont("TitledBorder.font");
    if (f == null) {
      f = UIManager.getFont("Label.font");
    }
    if (f == null) {
      f = new Font("Dialog", Font.PLAIN, 12);
    }
    return f;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Border border = getBorder();
    Font backupFont = g.getFont();
    Color backupColor = g.getColor();
    Rectangle backupClip = g.getClipBounds();
    try {
      Font font;
      if (c.getFont() instanceof FontUIResource) {
        font = getTextFont();
      }
      else {
        font = c.getFont();
      }
      String text = getText();
      if (!StringUtility.hasText(text)) {
        text = null;
      }
      Color color = getTextColor(c);
      g.setFont(font);
      FontMetrics fm = g.getFontMetrics();
      int descent = fm.getDescent();
      int ascent = fm.getAscent();
      int ascent2 = (ascent + descent) / 2;
      //
      if (text == null) {
        if (border != null) {
          border.paintBorder(c, g, x, y + ascent2, width, height - ascent2);
        }
      }
      else {
        int stringWidth = fm.stringWidth(text);
        Rectangle textRect = new Rectangle(x + SPACE_BEFORE - H_GAP_BEFORE, y, stringWidth + H_GAP_BEFORE + H_GAP_AFTER, ascent + descent + V_GAP);
        if (border != null) {
          g.setClip(new Rectangle(x, y, textRect.x - x, height).intersection(backupClip));
          border.paintBorder(c, g, x, y + ascent2, width, height - ascent2);
          g.setClip(new Rectangle(textRect.x + textRect.width, y, width - (textRect.x - textRect.width), height).intersection(backupClip));
          border.paintBorder(c, g, x, y + ascent2, width, height - ascent2);
          g.setClip(new Rectangle(textRect.x, textRect.y + textRect.height, textRect.width, height - textRect.height).intersection(backupClip));
          border.paintBorder(c, g, x, y + ascent2, width, height - ascent2);
        }
        g.setColor(color);
        g.setClip(textRect.intersection(backupClip));
        g.drawString(text, textRect.x + H_GAP_BEFORE, textRect.y + ascent);
      }
    }
    finally {
      g.setClip(backupClip);
      g.setFont(backupFont);
      g.setColor(backupColor);
    }
  }

  @Override
  public Insets getBorderInsets(Component c) {
    FontMetrics fm;
    int descent = 0;
    int ascent = 16;
    Border border = getBorder();
    Insets insets = new Insets(0, 0, 0, 0);
    if (border != null) {
      Insets i = border.getBorderInsets(c);
      insets.top = i.top;
      insets.right = i.right;
      insets.bottom = i.bottom;
      insets.left = i.left;
    }
    if (c == null || getText() == null || getText().equals("")) {
      /* nop */
    }
    else {
      Font font = getTextFont();
      fm = c.getFontMetrics(font);
      if (fm != null) {
        descent = fm.getDescent();
        ascent = fm.getAscent();
      }
      insets.top = Math.max(insets.top, ascent + descent + V_GAP);
    }
    // build maximal values
    insets.top = Math.max(insets.top, m_minInsets.top);
    insets.left = Math.max(insets.left, m_minInsets.left);
    insets.bottom = Math.max(insets.bottom, m_minInsets.bottom);
    insets.right = Math.max(insets.right, m_minInsets.right);
    return insets;
  }

  @Override
  public boolean isBorderOpaque() {
    return false;
  }

}
