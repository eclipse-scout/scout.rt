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
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.SwingConstants;

import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * @version 3.x Bug fix of swings FlowLayout: 1. Flow layout is not considering
 *          too small space for items when right aligning 5 buttons and there is
 *          not enough space, FlowLayout is accidentially painting outside to
 *          the left of the component 2. Each Item has same height, too large
 *          height is truncated, too small is extended 3. FlowLayout might be
 *          horizontal or vertical
 */

public class FlowLayoutEx extends AbstractLayoutManager2 implements SwingConstants {
  private int m_align;
  private int m_hgap;
  private int m_vgap;
  private int m_orientation;
  private boolean m_fillHorizontal;
  private boolean m_fillVertical;
  // cache
  private Component[] m_visibleComponents;
  private Dimension[][] m_visibleComponentSizes;
  private int m_preferredSpan;// horizontal:with, vertical:height

  public FlowLayoutEx() {
    this(CENTER, 5, 5);
  }

  public FlowLayoutEx(int align) {
    this(align, 5, 5);
  }

  public FlowLayoutEx(int align, int hgap, int vgap) {
    this(HORIZONTAL, align, hgap, vgap);
  }

  public FlowLayoutEx(int orientation, int align, int hgap, int vgap) {
    if (orientation == VERTICAL) {
      m_orientation = orientation;
    }
    else {
      m_orientation = HORIZONTAL;
    }
    m_hgap = hgap;
    m_vgap = vgap;
    m_align = align;
  }

  public int getAlignment() {
    return m_align;
  }

  public void setAlignment(int align) {
    m_align = align;
  }

  public int getHgap() {
    return m_hgap;
  }

  public void setHgap(int hgap) {
    this.m_hgap = hgap;
  }

  public int getVgap() {
    return m_vgap;
  }

  public void setVgap(int vgap) {
    this.m_vgap = vgap;
  }

  public boolean isFillHorizontal() {
    return m_fillHorizontal;
  }

  public void setFillHorizontal(boolean fillHorizontal) {
    m_fillHorizontal = fillHorizontal;
  }

  public boolean isFillVertical() {
    return m_fillVertical;
  }

  public void setFillVertical(boolean fillVertical) {
    m_fillVertical = fillVertical;
  }

  @Override
  protected void validateLayout(Container parent) {
    ArrayList<Component> visibleComponents = new ArrayList<Component>();
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      if (c.isVisible()) {
        visibleComponents.add(c);
      }
    }
    m_visibleComponents = visibleComponents.toArray(new Component[visibleComponents.size()]);
    m_visibleComponentSizes = new Dimension[m_visibleComponents.length][3];
    m_preferredSpan = 0;
    for (int i = 0; i < m_visibleComponents.length; i++) {
      m_visibleComponentSizes[i] = SwingLayoutUtility.getValidatedSizes(m_visibleComponents[i]);
      if (m_orientation == HORIZONTAL) {
        if (i > 0) {
          m_preferredSpan += m_hgap;
        }
        m_preferredSpan += m_visibleComponentSizes[i][SwingLayoutUtility.PREF].width;
      }
      else {
        if (i > 0) {
          m_preferredSpan += m_vgap;
        }
        m_preferredSpan += m_visibleComponentSizes[i][SwingLayoutUtility.PREF].height;
      }
    }
  }

  @Override
  protected Dimension getLayoutSize(Container parent, int sizeflag) {
    Dimension dim = new Dimension(0, 0);
    if (m_visibleComponents.length > 0) {
      for (int i = 0; i < m_visibleComponents.length; i++) {
        Dimension d = m_visibleComponentSizes[i][sizeflag];
        if (m_orientation == HORIZONTAL) {
          dim.height = Math.max(dim.height, d.height);
          if (i > 0) {
            dim.width += m_hgap;
          }
          dim.width += d.width;
        }
        else {
          dim.width = Math.max(dim.width, d.width);
          if (i > 0) {
            dim.height += m_vgap;
          }
          dim.height += d.height;
        }
      }
      Insets insets = parent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;
    }
    return dim;
  }

  @Override
  public void layoutContainer(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      /*
       * necessary as workaround for awt bug: when component does not change
       * size, its reported minimumSize, preferredSize and maximumSize are
       * cached instead of beeing calculated using layout manager
       */
      if (!SwingUtility.IS_JAVA_7_OR_GREATER) {
        SwingUtility.setZeroBounds(parent.getComponents());
      }
      else {
        for (Component c : parent.getComponents()) {
          Rectangle r = c.getBounds();
          c.setBounds(r.x, r.y, 0, 0);
        }
      }
      //
      int n = m_visibleComponents.length;
      Insets insets = parent.getInsets();
      int w = parent.getWidth() - (insets.left + insets.right);
      int h = parent.getHeight() - (insets.top + insets.bottom);
      //
      if (m_orientation == HORIZONTAL) {
        Dimension[] sizes = calculateHorizontalSizes(parent, w);
        int usedSpan = 0;
        for (Dimension d : sizes) {
          usedSpan += d.width;
        }
        usedSpan += Math.max(n - 1, 0) * m_hgap;
        //
        int x = insets.left;
        int y = insets.top;
        if (m_align == LEFT) {
          //
        }
        else if (m_align == CENTER) {
          x += Math.max(0, (w - usedSpan) / 2);
        }
        else {
          x += Math.max(0, w - usedSpan);
        }
        for (int i = 0; i < n; i++) {
          Component m = m_visibleComponents[i];
          int netHeight;
          if (isFillVertical()) {
            netHeight = h;
          }
          else {
            netHeight = Math.min(h, sizes[i].height);
          }
          m.setBounds(x, y, sizes[i].width, netHeight);
          x += sizes[i].width + m_hgap;
        }
      }
      else {
        Dimension[] sizes = calculateVerticalSizes(parent, h);
        int usedSpan = 0;
        for (Dimension d : sizes) {
          usedSpan += d.height;
        }
        usedSpan += Math.max(n - 1, 0) * m_vgap;
        //
        int x = insets.left;
        int y = insets.top;
        if (m_align == TOP) {
          //
        }
        else if (m_align == CENTER) {
          y += Math.max(0, (h - usedSpan) / 2);
        }
        else {
          y += Math.max(0, h - usedSpan);
        }
        for (int i = 0; i < n; i++) {
          Component m = m_visibleComponents[i];
          int netWidth;
          if (isFillHorizontal()) {
            netWidth = w;
          }
          else {
            netWidth = Math.min(w, sizes[i].width);
          }
          m.setBounds(x, y, netWidth, sizes[i].height);
          y += sizes[i].height + m_vgap;
        }
      }
    }
  }

  private Dimension[] calculateHorizontalSizes(Container parent, int widthInclusiveGaps) {
    int n = m_visibleComponents.length;
    int netWidth = widthInclusiveGaps - Math.max(n - 1, 0) * m_hgap;
    Dimension[] sizes = new Dimension[n];
    if (m_preferredSpan <= netWidth) {
      if (isFillHorizontal()) {
        int weights = 0;
        int pixels = netWidth - m_preferredSpan;
        for (int i = 0; i < n; i++) {
          Dimension pref = m_visibleComponentSizes[i][SwingLayoutUtility.PREF];
          Dimension max = m_visibleComponentSizes[i][SwingLayoutUtility.MAX];
          int w = Math.max(0, max.width - pref.width);
          weights += w;
        }
        for (int i = 0; i < n; i++) {
          Dimension d = m_visibleComponentSizes[i][SwingLayoutUtility.PREF].getSize();
          Dimension max = m_visibleComponentSizes[i][SwingLayoutUtility.MAX];
          int w = Math.max(0, max.width - d.width);
          if (weights > 0) {
            int dPix = pixels * w / weights;
            d.width += dPix;
            pixels -= dPix;
            weights -= w;
          }
          sizes[i] = d;
        }
      }
      else {
        for (int i = 0; i < n; i++) {
          Dimension d = m_visibleComponentSizes[i][SwingLayoutUtility.PREF].getSize();
          sizes[i] = d;
        }
      }
    }/*>*/
    else {
      int w = 0;
      for (int i = 0; i < n; i++) {
        Dimension d = m_visibleComponentSizes[i][SwingLayoutUtility.MIN].getSize();
        // when a component is not "tolerant" enough to shrink, force initial
        // size of w/n
        d.width = Math.min(d.width, netWidth / n);
        sizes[i] = d;
        w += d.width;
      }
      for (int i = 0; i < n && w < netWidth; i++) {
        int growPotential = Math.max(0, m_visibleComponentSizes[i][SwingLayoutUtility.PREF].width - sizes[i].width);
        int consumed = Math.min(netWidth - w, growPotential);
        if (consumed > 0) {
          sizes[i].width += consumed;
          w += consumed;
        }
      }
    }
    return sizes;
  }

  private Dimension[] calculateVerticalSizes(Container parent, int heightInclusiveGaps) {
    int n = m_visibleComponents.length;
    int netHeight = heightInclusiveGaps - Math.max(n - 1, 0) * m_vgap;
    Dimension[] sizes = new Dimension[n];
    if (m_preferredSpan <= netHeight) {
      if (isFillVertical()) {
        int weights = 0;
        int pixels = netHeight - m_preferredSpan;
        for (int i = 0; i < n; i++) {
          Dimension pref = m_visibleComponentSizes[i][SwingLayoutUtility.PREF];
          Dimension max = m_visibleComponentSizes[i][SwingLayoutUtility.MAX];
          int h = Math.max(0, max.height - pref.height);
          weights += h;
        }
        for (int i = 0; i < n; i++) {
          Dimension d = m_visibleComponentSizes[i][SwingLayoutUtility.PREF].getSize();
          Dimension max = m_visibleComponentSizes[i][SwingLayoutUtility.MAX];
          int h = Math.max(0, max.height - d.height);
          if (weights > 0) {
            int dPix = pixels * h / weights;
            d.height += dPix;
            pixels -= dPix;
            weights -= h;
          }
          sizes[i] = d;
        }
      }
      else {
        for (int i = 0; i < n; i++) {
          Dimension d = m_visibleComponentSizes[i][SwingLayoutUtility.PREF].getSize();
          sizes[i] = d;
        }
      }
    }
    else {
      int h = 0;
      for (int i = 0; i < n; i++) {
        Dimension d = m_visibleComponentSizes[i][SwingLayoutUtility.MIN].getSize();
        // when a component is not "tolerant" enough to shrink, force initial
        // size of w/n
        d.height = Math.min(d.height, netHeight / n);
        sizes[i] = d;
        h += d.height;
      }
      for (int i = 0; i < n && h < netHeight; i++) {
        int growPotential = Math.max(0, m_visibleComponentSizes[i][SwingLayoutUtility.PREF].height - sizes[i].height);
        int consumed = Math.min(netHeight - h, growPotential);
        if (consumed > 0) {
          sizes[i].height += consumed;
          h += consumed;
        }
      }
    }
    return sizes;
  }

}
