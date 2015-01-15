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
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.action.menu.ISwingContextMenuMarker;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;
import org.eclipse.scout.rt.ui.swing.ext.MenuArrowDownIcon;

/**
 *
 */
public class ContextMenuDecorationItem extends AbstractDecorationItem implements ISwingContextMenuMarker {

  private PropertyChangeListener m_scoutPropertyChangeListener;

  private Rectangle m_bounds;
  private IContextMenu m_scoutContextMenu;
  private IconGroup m_iconGroup = MenuArrowDownIcon.createMenuArrowDownIconGroup(new Insets(2, 1, 15, 1));

  /**
   * @param owner
   * @param environment
   */
  public ContextMenuDecorationItem(IContextMenu scoutContextMenu, JComponent owner, ISwingEnvironment environment) {
    super(owner, environment, false);
    m_scoutContextMenu = scoutContextMenu;
    init();
    setMouseOverCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  @Override
  protected void init() {
    super.init();
    if (getScoutContextMenu() != null && m_scoutPropertyChangeListener == null) {
      m_scoutPropertyChangeListener = new P_ScoutPropertyChangeListener();
      getScoutContextMenu().addPropertyChangeListener(m_scoutPropertyChangeListener);
    }
    updateContextMenuVisibility();
  }

  public void destroy() {
    if (m_scoutPropertyChangeListener != null) {
      getScoutContextMenu().removePropertyChangeListener(m_scoutPropertyChangeListener);
      m_scoutPropertyChangeListener = null;
    }
  }

  private void updateContextMenuVisibility() {
    setMarkerVisible(getScoutContextMenu().isVisible());
  }

  @Override
  protected Rectangle paintInternal(Component c, Graphics g, int x, int y) {
    if (!isVisible()) {
      return NULL_RECTANGLE;
    }
    Icon icon = getIconForState();
    icon.paintIcon(c, g, x, y);

    return new Rectangle(x, y, icon.getIconWidth(), icon.getIconHeight());
  }

  private int calcArrowWidth(int line) {
    return 5 - (line * 2);
  }

  @Override
  public int getWidth() {
    if (isVisible()) {
      return getIconForState().getIconWidth();
    }
    return 0;
  }

  @Override
  public int getHeight() {
    if (isVisible()) {
      return getIconForState().getIconHeight();
    }
    return 0;
  }

  public IContextMenu getScoutContextMenu() {
    return m_scoutContextMenu;
  }

  @Override
  public void setMarkerVisible(boolean visible) {
    setVisible(visible);
    getOwner().repaint();
  }

  @Override
  public boolean isMarkerVisible() {
    return isVisible();
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

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            updateContextMenuVisibility();
          }

        };
        getEnvironment().invokeSwingLater(t);
      }
    }
  }
}
