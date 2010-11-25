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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * <pre>
 * Fixed version of BorderLayout
 * Sun's BorderLayout does not respect maximumSize of contained components.
 * It just returns new Dimension(MAX,MAX)
 * 1) this class corrects this behaviour and respects container item's min, preferred and max sizes
 * 2) this class fixes issues when dimensions use Integer.MAX_VALUE
 * 3) fixes out-of-component layout when container is smaller than preferred size
 * </pre>
 */
public class BorderLayoutEx extends BorderLayout {
  private static final long serialVersionUID = 1L;

  public BorderLayoutEx() {
    super();
  }

  public BorderLayoutEx(int hgap, int vgap) {
    super(hgap, vgap);
  }

  protected Dimension getLayoutSize(Container parent, int sizeflag) {
    int hgap = getHgap();
    int vgap = getVgap();
    Dimension dim = new Dimension(0, 0);
    boolean ltr = parent.getComponentOrientation().isLeftToRight();
    Component c = null;
    if ((c = getChild(EAST, ltr)) != null) {
      Dimension d = SwingLayoutUtility.getSize(c, sizeflag);
      if (sizeflag == AbstractLayoutManager2.MAX_SIZE) {
        doMaxSizeRangeCheck(d);
      }
      dim.width += d.width + hgap;
      dim.height = Math.max(d.height, dim.height);
    }
    if ((c = getChild(WEST, ltr)) != null) {
      Dimension d = SwingLayoutUtility.getSize(c, sizeflag);
      if (sizeflag == AbstractLayoutManager2.MAX_SIZE) {
        doMaxSizeRangeCheck(d);
      }
      dim.width += d.width + hgap;
      dim.height = Math.max(d.height, dim.height);
    }
    if ((c = getChild(CENTER, ltr)) != null) {
      Dimension d = SwingLayoutUtility.getSize(c, sizeflag);
      if (sizeflag == AbstractLayoutManager2.MAX_SIZE) {
        doMaxSizeRangeCheck(d);
      }
      dim.width += d.width;
      dim.height = Math.max(d.height, dim.height);
    }
    if ((c = getChild(NORTH, ltr)) != null) {
      Dimension d = SwingLayoutUtility.getSize(c, sizeflag);
      if (sizeflag == AbstractLayoutManager2.MAX_SIZE) {
        doMaxSizeRangeCheck(d);
      }
      dim.width = Math.max(d.width, dim.width);
      dim.height += d.height + vgap;
    }
    if ((c = getChild(SOUTH, ltr)) != null) {
      Dimension d = SwingLayoutUtility.getSize(c, sizeflag);
      if (sizeflag == AbstractLayoutManager2.MAX_SIZE) {
        doMaxSizeRangeCheck(d);
      }
      dim.width = Math.max(d.width, dim.width);
      dim.height += d.height + vgap;
    }

    if (dim.width > 0 && dim.height > 0) {
      Insets insets = parent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;
    }

    return dim;
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      return new Dimension(getLayoutSize(parent, AbstractLayoutManager2.MIN_SIZE));
    }
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      return new Dimension(getLayoutSize(parent, AbstractLayoutManager2.PREF_SIZE));
    }
  }

  @Override
  public Dimension maximumLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      return new Dimension(getLayoutSize(parent, AbstractLayoutManager2.MAX_SIZE));
    }
  }

  protected void doMaxSizeRangeCheck(Dimension d) {
    if (d.width > 1000000) {
      d.width = 1000000;
    }
    if (d.height > 1000000) {
      d.height = 1000000;
    }
  }

  @Override
  public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      /*
       * necessary as workaround for awt bug: when component does not change
       * size, its reported minimumSize, preferredSize and maximumSize are
       * cached instead of beeing calculated using layout manager
       */
      for (Component c : parent.getComponents()) {
        c.setBounds(0, 0, 0, 0);
      }
      //
      Insets insets = parent.getInsets();
      int top = insets.top;
      int bottom = parent.getHeight() - insets.bottom;
      int left = insets.left;
      int right = parent.getWidth() - insets.right;
      int hgap = getHgap();
      int vgap = getVgap();

      boolean ltr = parent.getComponentOrientation().isLeftToRight();
      Component c = null;

      if ((c = getChild(NORTH, ltr)) != null) {
        c.setSize(right - left, c.getHeight());
        Dimension d = c.getPreferredSize();
        int ch = Math.max(0, Math.min(bottom - top, d.height));
        c.setBounds(left, top, right - left, ch);
        top += ch + vgap;
      }
      if ((c = getChild(SOUTH, ltr)) != null) {
        c.setSize(right - left, c.getHeight());
        Dimension d = c.getPreferredSize();
        int ch = Math.max(0, Math.min(bottom - top, d.height));
        c.setBounds(left, bottom - ch, right - left, ch);
        bottom -= ch + vgap;
      }
      if ((c = getChild(WEST, ltr)) != null) {
        c.setSize(c.getWidth(), bottom - top);
        Dimension d = c.getPreferredSize();
        int cw = Math.max(0, Math.min(right - left, d.width));
        c.setBounds(left, top, cw, bottom - top);
        left += cw + hgap;
      }
      if ((c = getChild(EAST, ltr)) != null) {
        c.setSize(c.getWidth(), bottom - top);
        Dimension d = c.getPreferredSize();
        int cw = Math.max(0, Math.min(right - left, d.width));
        c.setBounds(right - cw, top, cw, bottom - top);
        right -= cw + hgap;
      }
      if ((c = getChild(CENTER, ltr)) != null) {
        int cw = Math.max(0, right - left);
        int ch = Math.max(0, bottom - top);
        c.setBounds(left, top, cw, ch);
      }
    }
  }

  private Component getChild(String key, boolean ltr) {
    Component result = null;
    if (key == NORTH) {
      Component firstLine = getLayoutComponent(PAGE_START);
      Component north = getLayoutComponent(NORTH);
      result = (firstLine != null) ? firstLine : north;
    }
    else if (key == SOUTH) {
      Component lastLine = getLayoutComponent(PAGE_END);
      Component south = getLayoutComponent(SOUTH);
      result = (lastLine != null) ? lastLine : south;
    }
    else if (key == WEST) {
      Component firstItem = getLayoutComponent(LINE_START);
      Component lastItem = getLayoutComponent(LINE_END);
      Component west = getLayoutComponent(WEST);
      result = ltr ? firstItem : lastItem;
      if (result == null) {
        result = west;
      }
    }
    else if (key == EAST) {
      Component firstItem = getLayoutComponent(LINE_START);
      Component lastItem = getLayoutComponent(LINE_END);
      Component east = getLayoutComponent(EAST);
      result = ltr ? lastItem : firstItem;
      if (result == null) {
        result = east;
      }
    }
    else if (key == CENTER) {
      Component center = getLayoutComponent(CENTER);
      result = center;
    }
    if (result != null && !result.isVisible()) {
      result = null;
    }
    return result;
  }

}
