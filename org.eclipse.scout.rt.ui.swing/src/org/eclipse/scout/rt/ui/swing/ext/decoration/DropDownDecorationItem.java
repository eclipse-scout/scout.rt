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
package org.eclipse.scout.rt.ui.swing.ext.decoration;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;

/**
 *
 */
public class DropDownDecorationItem extends AbstractDecorationItem {

  private IconGroup m_iconGroup = new IconGroup();

  /**
   * @param owner
   * @param environment
   */
  public DropDownDecorationItem(JComponent owner, ISwingEnvironment environment) {
    super(owner, environment, true);
    setMouseOverCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  @Override
  protected void init() {
    super.init();
    m_iconGroup = new IconGroup(getEnvironment(), AbstractIcons.SmartFieldBrowse);
  }

  @Override
  protected Rectangle paintInternal(Component c, Graphics g, int x, int y) {
    if (!isVisible()) {
      return NULL_RECTANGLE;
    }
    int xx = x;
    int yy = y;
    int width = getWidth();
    int height = getHeight();
    // correction vertical alignment
    if (c.getHeight() > height) {
      yy = y;//(c.getHeight() - height) / 2;
    }
    getIconForState().paintIcon(c, g, xx, yy);
    return new Rectangle(xx, yy, width, height);
  }

  @Override
  public int getWidth() {
    if (!isVisible()) {
      return 0;
    }
    Icon icon = getIconForState();
    if (icon == null) {
      return 0;
    }
    return icon.getIconWidth();
  }

  @Override
  public int getHeight() {
    if (!isVisible()) {
      return 0;
    }
    Icon icon = getIconForState();
    if (icon == null) {
      return 0;
    }
    return icon.getIconHeight();
  }

  public void setIconGroup(IconGroup iconGroup) {
    if (iconGroup == null) {
      m_iconGroup = new IconGroup(getEnvironment(), AbstractIcons.SmartFieldBrowse);
    }
    else {
      m_iconGroup = iconGroup;
    }
  }

  private Icon getIconForState() {
    IconState iconState;
    switch (getState()) {
      case Disabled:
        iconState = IconState.DISABLED;
        break;
      case Rollover:
        iconState = IconState.ROLLOVER;
        break;
      case Selected:
        iconState = IconState.SELECTED;
        break;

      default:
        iconState = IconState.NORMAL;
        break;
    }
    return m_iconGroup.getIcon(iconState);
  }
}
