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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Custom widget for a button with drop-down menu.
 */
public class JDropDownButton extends JComponent {
  private static final long serialVersionUID = 1L;
  private static final String UI_CLASS_ID = "DropDownButtonUI";

  private AbstractButton m_pushButton;
  private JButton m_menuButton;
  private Icon enabledDownArrow, disDownArrow;

  public JDropDownButton(AbstractButton mainButton) {
    setOpaque(false);
    //
    m_pushButton = mainButton;
    m_pushButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          getMenuButton().doClick();
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          getMenuButton().doClick();
        }
      }
    });
    add(m_pushButton, BorderLayout.CENTER);
    //
    enabledDownArrow = new SmallDownArrow();
    disDownArrow = new SmallDisabledDownArrow();
    m_menuButton = new JButton(enabledDownArrow);
    m_menuButton.setDisabledIcon(disDownArrow);
    m_menuButton.setFocusPainted(false);
    m_menuButton.setFocusable(false);
    add(m_menuButton, BorderLayout.EAST);

    setLayout(new DropDownButtonLayout(m_pushButton, m_menuButton, 12));
    updateUI();
  }

  public AbstractButton getPushButton() {
    return m_pushButton;
  }

  public JButton getMenuButton() {
    return m_menuButton;
  }

  @Override
  public void setEnabled(boolean enable) {
    m_pushButton.setEnabled(enable);
  }

  @Override
  public boolean isEnabled() {
    return m_pushButton.isEnabled();
  }

  /**
   * An icon to draw a small downward-pointing arrow.
   */
  private static class SmallDownArrow implements Icon {

    Color arrowColor = Color.black;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(arrowColor);
      g.drawLine(x, y, x + 4, y);
      g.drawLine(x + 1, y + 1, x + 3, y + 1);
      g.drawLine(x + 2, y + 2, x + 2, y + 2);
    }

    @Override
    public int getIconWidth() {
      return 6;
    }

    @Override
    public int getIconHeight() {
      return 4;
    }
  }

  /**
   * An icon to draw a disabled small downward-pointing arrow.
   */
  private static class SmallDisabledDownArrow extends SmallDownArrow {

    public SmallDisabledDownArrow() {
      arrowColor = new Color(140, 140, 140);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      super.paintIcon(c, g, x, y);
      g.setColor(Color.white);
      g.drawLine(x + 3, y + 2, x + 4, y + 1);
      g.drawLine(x + 3, y + 3, x + 5, y + 1);
    }
  }

  @Override
  public String getUIClassID() {
    return UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    // since DropDownButtonUI is not supported by JRE default look and feel, we have to check if the current
    // l&f supports an UI for this widget.
    if (UIManager.get(UI_CLASS_ID) != null) {
      setUI(UIManager.getUI(this));
    }
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
