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
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.eclipse.scout.rt.ui.swing.ext.LookAndFeelUtility;

/**
 * activity
 */
public class DefaultActivityComponent extends JComponent implements ActivityComponent, FocusListener {
  private static final long serialVersionUID = 1L;

  private static Color defaultColor = new Color(0x88aaff);

  private int m_rowIndex;
  private String m_text;

  public DefaultActivityComponent(int rowIndex) {
    m_rowIndex = rowIndex;
    setToolTipText(m_text);
    setBackground(defaultColor);
    setRequestFocusEnabled(true);
    setFocusable(true);
    addFocusListener(this);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        requestFocus();
      }
    });
  }

  @Override
  public void focusGained(FocusEvent e) {
    repaint();
  }

  @Override
  public void focusLost(FocusEvent e) {
    repaint();
  }

  public String getText() {
    return m_text;
  }

  public void setText(String s) {
    m_text = s;
    repaint();
  }

  @Override
  public int getRowIndex() {
    return m_rowIndex;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Shape oldShape = g.getClip();
    try {
      // border
      g.setColor(getBackground());
      g.fill3DRect(1, 1, getWidth() - 2, getHeight() - 2, true);
      // focus
      if (isFocusOwner()) {
        g.setColor(UIManager.getColor("Table.focusCellForeground"));
        LookAndFeelUtility.drawFocus(g, 1, 1, getWidth() - 2, getHeight() - 2);
      }
      // text
      g.setColor(getBackground());
      g.setFont(getFont());
      String s = m_text;
      if (s != null) {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(getForeground());
        int sWidth = fm.stringWidth(s);
        int dx = Math.max(0, getWidth() - 4 - sWidth);
        // centered
        g.clipRect(2, 2, getWidth() - 4, getHeight() - 4);
        g.drawString(s, 2 + dx / 2, getHeight() - 2 - fm.getDescent());
      }
    }
    finally {
      g.setClip(oldShape);
    }
  }
}
