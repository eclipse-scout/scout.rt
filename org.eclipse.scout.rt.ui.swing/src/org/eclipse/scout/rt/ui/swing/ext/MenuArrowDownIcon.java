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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;

/**
 *
 */
public class MenuArrowDownIcon implements Icon {
  private static final Map<IconState, Color> ARROW_COLOR_MAP = new HashMap<IconState, Color>();
  {
    ARROW_COLOR_MAP.put(IconState.NORMAL, UIManager.getColor("TextFieldArrow.normal"));
    ARROW_COLOR_MAP.put(IconState.DISABLED, UIManager.getColor("TextFieldArrow.disabled"));
    ARROW_COLOR_MAP.put(IconState.ROLLOVER, UIManager.getColor("TextFieldArrow.mouseOver"));
  }

  private Insets m_insets;
  private IconState m_state;

  public MenuArrowDownIcon(Insets insets, IconState state) {
    m_insets = insets;
    m_state = state;
  }

  @Override
  public int getIconWidth() {
    int width = 5;
    if (getInsets() != null) {
      width += getInsets().left;
      width += getInsets().right;
    }
    return width;
  }

  @Override
  public int getIconHeight() {
    int height = 3;
    if (getInsets() != null) {
      height += getInsets().top + getInsets().bottom;
    }
    return height;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    int arrowHeight = 3;
    // 1
    int startAtX = x;
    // 2
    int startAtY = y;
    if (getInsets() != null) {
      startAtX += getInsets().left;
      startAtY += getInsets().top;
    }
    g.setColor(ARROW_COLOR_MAP.get(m_state));
    for (int j = 0; j < arrowHeight; j++) {
      int arrowWidth = calcArrowWidth(j);
      int ax = startAtX + j;
      int ay = startAtY + j;
      g.drawLine(ax, ay, ax + arrowWidth - 1, ay);
    }
  }

  private int calcArrowWidth(int line) {
    return 5 - (line * 2);
  }

  public Insets getInsets() {
    return m_insets;
  }

  public static IconGroup createMenuArrowDownIconGroup(Insets insets) {
    IconGroup group = new IconGroup();
    group.setIcon(IconState.NORMAL, new MenuArrowDownIcon(insets, IconState.NORMAL));
    group.setIcon(IconState.ROLLOVER, new MenuArrowDownIcon(insets, IconState.ROLLOVER));
    group.setIcon(IconState.DISABLED, new MenuArrowDownIcon(insets, IconState.DISABLED));
    return group;
  }
}
