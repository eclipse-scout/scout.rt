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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * {@link CheckboxIcon} with margin support
 */
public class CheckboxWithMarginIcon extends CheckboxIcon {

  private Insets m_insets;

  public CheckboxWithMarginIcon(Insets insets) {
    if (insets != null) {
      m_insets = insets;
    }
    else {
      m_insets = new Insets(0, 0, 0, 0);
    }
  }

  public CheckboxWithMarginIcon() {
    m_insets = new Insets(0, 0, 0, 0);
  }

  @Override
  public int getIconHeight() {
    return m_insets.top + super.getIconHeight() + m_insets.bottom;
  }

  @Override
  public int getIconWidth() {
    return m_insets.left + super.getIconWidth() + m_insets.right;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics g2 = g.create();
    g2.translate(x + m_insets.left, y + m_insets.top);
    super.paintIcon(c, g2, 0, 0);
  }
}
