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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager2;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * Widget for view tabs.
 * This widget simplifies presentation of a label which changes its text between plain, bold and underlined.
 * It avoids the problem, that a plain text is less wide than bold text (that's why a JButton is not good
 * enough, since it changes its size every time the font changes).
 * Furthermore with a JButton the only way to underline text is to use HTML text. But when you use HTML text
 * on a JButton it ignores the foreground color unless you set the color attribute of the font tag in the HTML
 * text (which you have to do manually, eek).
 * 
 * @author awe
 */
public class JViewTab extends AbstractJTab {

  private static final long serialVersionUID = 1L;

//XXX make styling in L&F with UI type and in synth using its config xml
  private static final Font FONT_DEFAULT = UIManager.getFont("Synth.ViewTab.font");

  private static final Font FONT_SELECTED = UIManager.getFont("Synth.ViewTab.fontSelected");

  private static final Color COLOR_DEFAULT = UIManager.getColor("Synth.ViewTab.foreground");

  private static final Color COLOR_SELECTED = UIManager.getColor("Synth.ViewTab.foregroundSelected");

  int[] normalFontSize;

  int[] activeFontSize;

  public JViewTab(ISwingEnvironment env) {
    super(env);
    setName("Synth.ViewTab");
    setLayout(new Layout());
    normalFontSize = measureFont(FONT_DEFAULT);
    activeFontSize = measureFont(FONT_SELECTED);
  }

  @Override
  public Dimension getMinimumSize() {
    return getLayout().minimumLayoutSize(this);
  }

  @Override
  public Dimension getPreferredSize() {
    return getLayout().preferredLayoutSize(this);
  }

  @Override
  public Dimension getMaximumSize() {
    return ((LayoutManager2) getLayout()).maximumLayoutSize(this);
  }

  int[] measureFont(Font font) {
    if (font == null) {
      font = getFont();
    }
    FontMetrics fm = getFontMetrics(font);
    int width = fm.stringWidth(getText());
    int height = fm.getHeight();
    int baseline = fm.getAscent();
    return new int[]{width, height, baseline};
  }

  @Override
  public void setIconGroupById(String iconId) {
    //no icons on view tabs
  }

  @Override
  protected void paintComponent(Graphics g) {
    Insets insets = getInsets();
    Icon icon = getIconForTabState();
    Insets iconInsets = new Insets(0, 0, 0, 0);
    if (icon != null) {
      iconInsets.left = icon.getIconWidth() + 2;
      icon.paintIcon(this, g, insets.left, insets.top);
    }
    int centerTextOffset = 0;
    int normalWidth = normalFontSize[0];
    int activeWidth = activeFontSize[0];
    int yBaseline = insets.top + iconInsets.top + normalFontSize[2] + Math.max(0, (getHeight() - normalFontSize[1] - insets.top - insets.bottom - iconInsets.top - iconInsets.bottom) / 2);
    if (isSelected()) {
      g.setFont(FONT_SELECTED);
      g.setColor(COLOR_SELECTED);
    }
    else {
      g.setFont(FONT_DEFAULT);
      g.setColor(COLOR_DEFAULT);
      centerTextOffset = (activeWidth - normalWidth) / 2;
      if (isMouseOver()) {
        int x = insets.left + iconInsets.left + centerTextOffset;
        int y = yBaseline + 1;
        g.drawLine(x, y, x + normalWidth - 1, y);
      }
    }
    int x = insets.left + iconInsets.left + centerTextOffset;
    int y = yBaseline;
    g.drawString(getText(), x, y);
  }

  private class Layout extends AbstractLayoutManager2 {
    private Dimension m_size;

    @Override
    protected void validateLayout(Container parent) {
      normalFontSize = measureFont(FONT_DEFAULT);
      activeFontSize = measureFont(FONT_SELECTED);
      int w = activeFontSize[0];
      int h = activeFontSize[1];
      Icon icon = getIconForTabState();
      if (icon != null) {
        w += icon.getIconWidth();
        w += 2;
        h = Math.max(h, icon.getIconHeight());
      }
      Insets insets = getInsets();
      if (insets != null) {
        w += insets.left + insets.right;
        h += insets.top + insets.bottom;
      }
      m_size = new Dimension(w, h);
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      return m_size;
    }

    @Override
    public void layoutContainer(Container parent) {
      //nop
    }
  }
}
