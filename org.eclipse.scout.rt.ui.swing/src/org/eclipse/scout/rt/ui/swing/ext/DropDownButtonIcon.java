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
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;

/**
 * The DropDownButtonIcon is used within the JTextFieldWithIcon as a replacement for JDropDownButton.
 */
public class DropDownButtonIcon implements Icon {

  public enum MouseOver {
    NONE,
    ICON,
    ARROW
  }

  private static final Map<IconState, Color> ARROW_COLOR_MAP = new HashMap<IconState, Color>();

  {
    ARROW_COLOR_MAP.put(IconState.NORMAL, UIManager.getColor("TextFieldArrow.normal"));
    ARROW_COLOR_MAP.put(IconState.DISABLED, UIManager.getColor("TextFieldArrow.disabled"));
    ARROW_COLOR_MAP.put(IconState.ROLLOVER, UIManager.getColor("TextFieldArrow.mouseOver"));
  }

  private class P_IconPart {
    private boolean m_visible = true;
    private boolean m_enabled = true;
    private boolean m_mouseOver = false;

    public boolean isVisible() {
      return m_visible;
    }

    public void setVisible(boolean visible) {
      m_visible = visible;
    }

    public IconState getIconState() {
      if (!m_enabled) {
        return IconState.DISABLED;
      }
      else if (m_mouseOver) {
        return IconState.ROLLOVER;
      }
      else {
        return IconState.NORMAL;
      }
    }

    public boolean isEnabled() {
      return m_enabled;
    }

    public void setEnabled(boolean b) {
      m_enabled = b;
    }

    public boolean isMouseOver() {
      return m_mouseOver;
    }

    public void setMouseOver(boolean b) {
      m_mouseOver = b;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() +
          "[enabled=" + m_enabled +
          " mouseOver=" + m_mouseOver +
          " visible=" + m_visible + "]";
    }
  }

  private final ISwingEnvironment m_env;
  private P_IconPart m_partIcon = new P_IconPart();
  private P_IconPart m_partArrow = new P_IconPart();
  private IconGroup m_iconGroup = new IconGroup();

  public DropDownButtonIcon(ISwingEnvironment env) {
    m_env = env;
    m_partArrow.setEnabled(false);
    m_partArrow.setVisible(false);
    m_iconGroup = new IconGroup(m_env, AbstractIcons.SmartFieldBrowse);
  }

  public void setIconGroup(IconGroup iconGroup) {
    if (iconGroup == null) {
      m_iconGroup = new IconGroup(m_env, AbstractIcons.SmartFieldBrowse);
    }
    else {
      m_iconGroup = iconGroup;
    }
  }

  public boolean isArrowEnabled() {
    return m_partArrow.isEnabled();
  }

  public void setArrowEnabled(boolean enabled) {
    m_partArrow.setEnabled(enabled);
    m_partArrow.setVisible(enabled);
  }

  public boolean isIconEnabled() {
    return m_partIcon.isEnabled();
  }

  public void setIconEnabled(boolean enabled) {
    m_partIcon.setEnabled(enabled);
  }

  @Override
  public int getIconHeight() {
    return getIconForState().getIconHeight();
  }

  @Override
  public int getIconWidth() {
    return getIconForState().getIconWidth();
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    getIconForState().paintIcon(c, g, x, y);
    paintArrow(c, g, x, y);
  }

  private Icon getIconForState() {
    return m_iconGroup.getIcon(m_partIcon.getIconState());
  }

  private void paintArrow(Component c, Graphics g, int x, int y) {
    if (!m_partArrow.isVisible()) {
      return;
    }
    int arrowHeight = 3;
    int maxArrowWidth = 5;
    int startAtX = x + getIconWidth() - maxArrowWidth;
    int startAtY = y + 2;
    g.setColor(ARROW_COLOR_MAP.get(m_partArrow.getIconState()));
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

  public void setMouseOver(MouseOver mouseOver) {
    if (MouseOver.NONE == mouseOver) {
      m_partIcon.setMouseOver(false);
      m_partArrow.setMouseOver(false);
    }
    else if (MouseOver.ICON == mouseOver) {
      m_partIcon.setMouseOver(true);
      m_partArrow.setMouseOver(false);
    }
    else if (MouseOver.ARROW == mouseOver) {
      m_partIcon.setMouseOver(false);
      m_partArrow.setMouseOver(true);
    }
  }
}
