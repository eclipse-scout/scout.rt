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
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;

/**
 * Custom widget for a button with drop-down menu.
 */
public class JButtonWithMenu extends JComponent {
  private static final long serialVersionUID = 1L;
  private static final String UI_CLASS_ID = "ButtonWithMenuUI";

  private AbstractButton m_pushButton;
  private JButton m_menuButton;

  public JButtonWithMenu(AbstractButton mainButton) {
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
    m_menuButton = new JButton();
    IconGroup iconGroup = MenuArrowDownIcon.createMenuArrowDownIconGroup(new Insets(1, 2, 15, 1));
    m_menuButton.setIcon(iconGroup.getIcon(IconState.NORMAL));
    m_menuButton.setRolloverIcon(iconGroup.getIcon(IconState.ROLLOVER));
    m_menuButton.setDisabledIcon(iconGroup.getIcon(IconState.DISABLED));
    m_menuButton.setFocusPainted(false);
    m_menuButton.setFocusable(false);
    m_menuButton.setBorder(BorderFactory.createEmptyBorder());
    add(m_menuButton, BorderLayout.EAST);

    FlowLayoutEx layout = new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, 0, 0);
    setLayout(layout);// new DropDownButtonLayout(m_pushButton, m_menuButton, 12));
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
