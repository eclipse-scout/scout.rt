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
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

public class ActivityMapLayout implements LayoutManager2 {

  @Override
  public void addLayoutComponent(String name, Component comp) {
    addLayoutComponent(comp, name);
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
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
    return new Dimension(20, 20);
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      JActivityMap map = (JActivityMap) parent;
      int rowCount = map.getRowCount();
      Rectangle r = map.getCellRect(0, rowCount - 1, new double[]{0, 1});
      r.width = 800;
      return new Dimension(r.width, r.height);
    }
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(10240, 10240);
  }

  @Override
  public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      JActivityMap map = (JActivityMap) parent;
      for (Component c : parent.getComponents()) {
        if (c.isVisible()) {
          if (c instanceof JSelector) {
            double[] range = map.getSelectedRange();
            if (range != null) {
              Rectangle r = map.getRect(map.getSelectedRange());
              r.y = 0;
              r.height = parent.getHeight();
              c.setBounds(r);
            }
            else {
              c.setBounds(-1, -1, 0, 0);
            }
          }
          else if (c instanceof ActivityComponent) {
            ActivityComponent a = (ActivityComponent) c;
            Rectangle r = map.getCellRect(a.getRowIndex(), a.getRowIndex(), map.getModel().getActivityRange(a));
            c.setBounds(r);
          }
          else {
            c.setBounds(-1, -1, 0, 0);
            System.out.println("Unexpected component on JActivityMap: " + c);
          }
        }
      }
    }
  }

}
