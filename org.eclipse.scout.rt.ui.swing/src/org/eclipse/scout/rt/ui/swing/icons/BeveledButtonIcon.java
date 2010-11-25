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
package org.eclipse.scout.rt.ui.swing.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.Icon;

public class BeveledButtonIcon implements Icon {
  private AbstractButton m_button;
  private boolean m_raised;

  public BeveledButtonIcon(AbstractButton button, boolean raised) {
    m_button = button;
    m_raised = raised;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Icon basicIcon = m_button.getIcon();
    if (basicIcon != null) {
      if (m_raised) {
        basicIcon.paintIcon(c, g, x, y);
      }
      else {
        basicIcon.paintIcon(c, g, x + 1, y + 1);
      }
      g.setColor(Color.lightGray);
      g.draw3DRect(x, y, getIconWidth(), getIconHeight(), m_raised);
    }
  }

  public int getIconWidth() {
    Icon basicIcon = m_button.getIcon();
    if (basicIcon != null) {
      return basicIcon.getIconWidth();
    }
    else {
      return 0;
    }
  }

  public int getIconHeight() {
    Icon basicIcon = m_button.getIcon();
    if (basicIcon != null) {
      return basicIcon.getIconHeight();
    }
    else {
      return 0;
    }
  }
}
