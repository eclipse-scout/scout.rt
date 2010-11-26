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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.JNavigationWidget.ButtonName;

public class ForwardButton extends NavigationWidgetButton {

  Icon[] m_historyIcon;

  Point m_historyIconPos;

  private boolean historyEnabled = false;

  public ForwardButton(JComponent parent) {
    super(ButtonName.FORWARD, parent);
  }

  public void loadHistoryIcon(String iconUrl) {
    m_historyIcon = loadTriStateIcon(iconUrl);
  }

  public boolean isHistoryEnabled() {
    return historyEnabled;
  }

  public void setHistoryEnabled(boolean historyEnabled) {
    this.historyEnabled = historyEnabled;
  }

  public Icon getHistoryIcon() {
    return getTriStateIcon(m_historyIcon, isHistoryEnabled());
  }

  @Override
  protected void paintIcon(Component c, Graphics g) {
    super.paintIcon(c, g);
    paintPressedImage(getHistoryIcon(), m_historyIconPos, isPressed(), c, g);
  }

  @Override
  public void buttonClicked(int button) {
    switch (button) {
      case MouseEvent.BUTTON1:
        performAction(getPrimaryAction());
        break;
      case MouseEvent.BUTTON3:
        if (isHistoryEnabled()) {
          performAction(getSecondaryAction());
        }
        break;
      default:
    }
  }
}
