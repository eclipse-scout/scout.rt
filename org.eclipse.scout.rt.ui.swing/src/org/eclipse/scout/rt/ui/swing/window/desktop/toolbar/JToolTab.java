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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

public class JToolTab extends AbstractJTab {

  private static final long serialVersionUID = 1L;
  private static final int VERTICAL_SHIFT = 2;

  public static final String PROP_CHANGED_TOOL_TAB_STATE = "changedToolTabState";

  public JToolTab(ISwingEnvironment env) {
    super(env);
    setOpaque(false);
    setName("Synth.ToolTab");
    setLayout(new Layout());

    // <bsh 2010-10-15>
    // Override the default button model provided by AbstractJTab. When the selection state of
    // a tool tab changes, we always want to check if the whole bar should be collapsed.
    setModel(new ToggleButtonModel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void setSelected(boolean b) {
        super.setSelected(b);
        firePropertyChange(PROP_CHANGED_TOOL_TAB_STATE, !b, b);
      }
    });
    // </bsh>
  }

  @Override
  public Dimension getMinimumSize() {
    return getLayout().minimumLayoutSize(this);
  }

  @Override
  public Dimension getPreferredSize() {
    return getLayout().preferredLayoutSize(this);
  }

  @Override
  public Dimension getMaximumSize() {
    return ((LayoutManager2) getLayout()).maximumLayoutSize(this);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Insets insets = getInsets();
    int pixelShift = isSelected() ? VERTICAL_SHIFT : 1;
    Icon icon = getIconForTabState();
    if (icon != null) {
      icon.paintIcon(this, g, insets.left, insets.top + pixelShift + 2);
    }
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }

  private class Layout extends AbstractLayoutManager2 {
    private Dimension m_size;

    @Override
    protected void validateLayout(Container parent) {
      Dimension d = new Dimension(0, 0);
      Icon icon = getIconForTabState();
      if (icon != null) {
        d.width = icon.getIconWidth();
        d.height = icon.getIconHeight();
      }
      Insets insets = getInsets();
      if (insets != null) {
        d.width += insets.left + insets.right;
        d.height += insets.top + insets.bottom;
      }
      m_size = d;
    }

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      return m_size;
    }

    @Override
    public void layoutContainer(Container parent) {
      //nop
    }
  }
}
