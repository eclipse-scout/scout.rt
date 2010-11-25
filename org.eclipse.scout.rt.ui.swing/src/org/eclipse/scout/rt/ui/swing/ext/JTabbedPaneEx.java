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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Bug fixes in JTabbedPane - sets all non-active tabs to non-focusable -
 * setting background to "decent" colors (empty space = same color as component)
 * - disable: do not disable tabbed GUI, only tabbed model - correct focus
 * handling, tab follows tab icons, then tab content
 */
public class JTabbedPaneEx extends JTabbedPane implements ChangeListener {
  private static final long serialVersionUID = 1L;

  public JTabbedPaneEx() {
    super();
    SwingUtility.installDefaultFocusHandling(this);
    addChangeListener(this);
  }

  @Override
  public Dimension getMinimumSize() {
    Dimension m = new Dimension(super.getMinimumSize());
    Dimension p = super.getPreferredSize();
    if (m.width > p.width) m.width = p.width;
    if (m.height > p.height) m.height = p.height;
    return m;
  }

  @Override
  protected void addImpl(Component comp, Object constraints, int index) {
    comp.setFocusable(false);
    super.addImpl(comp, constraints, index);
  }

  /**
   * Implementation of ChangeListener sets all non-active tabs to non-focusable
   */
  public void stateChanged(ChangeEvent e) {
    Component selectedTab = getSelectedComponent();
    for (int i = 0, n = getTabCount(); i < n; i++) {
      Component comp = getComponentAt(i);
      comp.setFocusable(comp == selectedTab);
    }
  }

  /**
   * Missing support in swing for setting background to "decent" colors
   */
  @Override
  public Color getBackgroundAt(int index) {
    Color c = super.getBackgroundAt(index);
    if (super.getSelectedIndex() == index) {
      return c;
    }
    else {
      return new Color(
          Math.max(0, c.getRed() - 48),
          Math.max(0, c.getGreen() - 48),
          Math.max(0, c.getBlue() - 48));
    }
  }

  /**
   * override: do not disable tabbed GUI, only tabbed model
   */
  @Override
  public void setEnabled(boolean b) {
    // nop
  }

  /**
   * Missing extension point in Swing for setting font per tabbed panes tab -->
   * deactivating text and painting text using an icon
   */
  @Override
  public String getTitleAt(int index) {
    return "";
  }

  /**
   * BUG in swing does not correctly layout and repaint if multiple tabs are
   * used
   */
  @Override
  public void doLayout() {
    super.doLayout();
    Component selected = getSelectedComponent();
    for (int i = 0, n = this.getComponentCount(); i < n; i++) {
      Component comp = this.getComponent(i);
      if (comp != selected) {
        comp.setBounds(0, 0, 0, 0);
      }
      else {
        // nop
      }
    }
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
