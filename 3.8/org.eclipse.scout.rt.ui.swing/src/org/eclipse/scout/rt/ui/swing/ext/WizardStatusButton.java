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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class WizardStatusButton extends JComponent {
  private static final long serialVersionUID = 1L;
  private static final int HEIGHT = 42;
  private static final int MIN_WIDTH = 160;

  private String m_index;
  private String m_text;
  private boolean m_selected;

  public WizardStatusButton() {
    setBorder(new EmptyBorder(3, 10, 3, 10));
    setMaximumSize(new Dimension(10240, HEIGHT));
    setForeground(new Color(0x120F78));
  }

  public String getIndex() {
    return m_index;
  }

  public void setIndex(String index) {
    this.m_index = index;
  }

  public String getText() {
    return m_text;
  }

  public void setText(String text) {
    this.m_text = text;
  }

  public boolean isSelected() {
    return m_selected;
  }

  public void setSelected(boolean selected) {
    this.m_selected = selected;
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getMaximumSize() {
    return new Dimension(10240, 10240);
  }

  @Override
  public Dimension getPreferredSize() {
    int w = MIN_WIDTH;
    if (m_text != null) {
      int textWidth = getFontMetrics(getFont()).stringWidth(m_text);
      Insets insets = getInsets();
      w = Math.max(insets.left + insets.right + HEIGHT + 6 + textWidth, MIN_WIDTH);
    }
    return new Dimension(w, HEIGHT);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Insets insets = getInsets();
    int w = getWidth() - insets.left - insets.right - 1;
    int h = getHeight() - insets.top - insets.bottom - 1;
    try {
      g.translate(insets.left, insets.top);
      // button
      if (isSelected()) {
        g2.setPaint(new GradientPaint(0, 0, new Color(0xFFE9CF), w, 0, new Color(0xFFB04D)));
      }
      else {
        g2.setPaint(new GradientPaint(0, 0, new Color(0x9CCCD9), w, 0, new Color(0x3E9CB4)));
      }
      g2.fillRoundRect(0, 0, w, h, h, h);

      // button border
      if (isSelected()) {
        g2.setPaint(new Color(0xFFB04D));
      }
      else {
        g2.setPaint(new Color(0x38EA9));
      }
      g2.drawRoundRect(0, 0, w, h, h, h);

      // circle
      if (isSelected()) {
        g2.setPaint(new Color(0xFFB04D));
      }
      else {
        g2.setPaint(new Color(0x38EA9));
      }
      g2.fillRoundRect(0, 0, h, h, h, h);

      // circle border
      if (isSelected()) {
        g2.setPaint(new Color(0xFFAA40));
      }
      else {
        g2.setPaint(new Color(0x0B82A0));
      }
      g2.drawRoundRect(0, 0, h, h, h, h);

      // index and text
      FontMetrics fm = g.getFontMetrics();
      int baseline = h / 2 + fm.getAscent() / 2;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      if (isSelected()) {
        g2.setPaint(new Color(0x000000));
      }
      else {
        g2.setPaint(new Color(0xFFFFFF));
      }
      if (m_index != null) {
        g2.drawString(m_index, (h - fm.stringWidth(m_index)) / 2 + 1, baseline);
      }
      if (m_text != null) {
        g2.drawString(m_text, h + 6, baseline);
      }
    }
    finally {
      g.translate(-insets.left, -insets.top);
    }
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
