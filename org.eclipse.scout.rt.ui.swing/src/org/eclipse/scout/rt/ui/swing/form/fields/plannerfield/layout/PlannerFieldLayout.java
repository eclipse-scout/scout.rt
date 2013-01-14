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
package org.eclipse.scout.rt.ui.swing.form.fields.plannerfield.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.HashMap;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * Layout of items in the following order: LABEL COMP1 COMP2 COMP3 COMP4 ... the
 * label is fixed sized
 */
public class PlannerFieldLayout extends AbstractLayoutManager2 {
  private HashMap<Component, PlannerFieldLayoutConstraints> m_constraints = new HashMap<Component, PlannerFieldLayoutConstraints>();
  private SwingScoutGridData m_sizes;

  public PlannerFieldLayout(ISwingEnvironment env, GridData gd) {
    m_sizes = new SwingScoutGridData(env, gd);
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
    throw new UnsupportedOperationException("use add(Component comp, Object constraints)");
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
    if (constraints instanceof PlannerFieldLayoutConstraints) {
      m_constraints.put(comp, (PlannerFieldLayoutConstraints) constraints);
    }
    else {
      throw new IllegalArgumentException("constraints must be of type RangeLayoutConstraints");
    }
  }

  @Override
  public void removeLayoutComponent(Component comp) {
    m_constraints.remove(comp);
  }

  @Override
  protected void validateLayout(Container parent) {
  }

  @Override
  protected Dimension getLayoutSize(Container parent, int sizeflag) {
    return m_sizes.getLayoutSize(sizeflag);
  }

  @Override
  public void layoutContainer(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      Component centerComponent = null;
      Component eastComponent = null;
      for (Component c : parent.getComponents()) {
        /*
         * necessary as workaround for awt bug: when component does not change
         * size, its reported minimumSize, preferredSize and maximumSize are
         * cached instead of beeing calculated using layout manager
         */
        if (!SwingUtility.IS_JAVA_7_OR_GREATER && SwingUtility.DO_RESET_COMPONENT_BOUNDS) {
          SwingUtility.setZeroBounds(c);
        }
        //
        PlannerFieldLayoutConstraints cons = m_constraints.get(c);
        if (cons != null) {
          if (cons.fieldType == PlannerFieldLayoutConstraints.PLANNER) {
            centerComponent = c;
          }
          else if (cons.fieldType == PlannerFieldLayoutConstraints.MINI_CALENDARS) {
            eastComponent = c;
          }
        }
      }

      Dimension size = parent.getSize();
      int gap = 2;
      int wCenter = 0;
      int wEast = 0;
      if (centerComponent != null && eastComponent != null) {
        Dimension[] centerSizes = SwingLayoutUtility.getValidatedSizes(centerComponent);
        Dimension[] eastSizes = SwingLayoutUtility.getValidatedSizes(eastComponent);
        if (centerSizes[1].width + gap + eastSizes[1].width <= size.width) {
          wEast = eastSizes[1].width;
          wCenter = size.width - gap - wEast;
        }
        else if (centerSizes[1].width + gap + eastSizes[0].width <= size.width) {
          wCenter = centerSizes[1].width;
          wEast = size.width - gap - wCenter;
        }
        else if (centerSizes[0].width + gap + eastSizes[0].width <= size.width) {
          wEast = eastSizes[0].width;
          wCenter = size.width - gap - wEast;
        }
        else if (centerSizes[0].width + gap + eastSizes[0].width <= size.width) {
          wCenter = centerSizes[0].width;
          wEast = Math.max(0, size.width - gap - wCenter);
        }
      }
      if (centerComponent != null) {
        centerComponent.setBounds(0, 0, wCenter, size.height);
      }
      if (eastComponent != null) {
        eastComponent.setBounds(wCenter + gap, 0, wEast, size.height);
      }
    }
  }

}
