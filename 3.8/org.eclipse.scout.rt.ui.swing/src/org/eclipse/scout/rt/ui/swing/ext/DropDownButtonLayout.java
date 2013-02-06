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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

import javax.swing.AbstractButton;

public class DropDownButtonLayout implements LayoutManager2 {

  private int m_menuWidth = 0;
  private AbstractButton m_push;
  private AbstractButton m_menu;

  public DropDownButtonLayout(AbstractButton push, AbstractButton menu, int menuWidth) {
    m_push = push;
    m_menu = menu;
    m_menuWidth = menuWidth;
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
  }

  @Override
  public void removeLayoutComponent(Component comp) {
  }

  @Override
  public float getLayoutAlignmentX(Container target) {
    return 0;
  }

  @Override
  public float getLayoutAlignmentY(Container target) {
    return 0;
  }

  @Override
  public void invalidateLayout(Container target) {
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    Dimension d = m_push.getMinimumSize();
    d.width += m_menuWidth;
    return d;
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    Dimension d = m_push.getPreferredSize();
    d.width += m_menuWidth;
    return d;
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    Dimension d = m_push.getMaximumSize();
    d.width += m_menuWidth;
    return d;
  }

  @Override
  public void layoutContainer(Container parent) {
    Dimension size = parent.getSize();
    m_push.setBounds(0, 0, size.width - m_menuWidth, size.height);
    m_menu.setBounds(size.width - m_menuWidth, 0, m_menuWidth, size.height);
  }
}
