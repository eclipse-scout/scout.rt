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
package org.eclipse.scout.rt.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

public class SingleLayout extends AbstractLayoutManager2 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SingleLayout.class);
  private Dimension[] m_sizes;

  @Override
  public float getLayoutAlignmentX(Container parent) {
    return 0.5f;
  }

  @Override
  public float getLayoutAlignmentY(Container parent) {
    return 0.5f;
  }

  @Override
  protected Dimension getLayoutSize(Container parent, int sizeflag) {
    return m_sizes[sizeflag];
  }

  @Override
  protected void validateLayout(Container parent) {
    m_sizes = null;
    for (Component c : parent.getComponents()) {
      if (c.isVisible()) {
        m_sizes = SwingLayoutUtility.getValidatedSizes(c);
        for (int sizeflag = 0; sizeflag < 3; sizeflag++) {
          Insets i = parent.getInsets();
          m_sizes[sizeflag].width += i.left + i.right;
          m_sizes[sizeflag].height += i.top + i.bottom;
        }
      }
    }
    if (m_sizes == null) {
      m_sizes = new Dimension[]{new Dimension(), new Dimension(), new Dimension()};
    }
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
      if (!SwingUtility.IS_JAVA_7_OR_GREATER && SwingUtility.DO_RESET_COMPONENT_BOUNDS) {
        SwingUtility.setZeroBounds(parent.getComponents());
      }

      for (Component c : parent.getComponents()) {
        if (c.isVisible()) {
          Rectangle r = new Rectangle(new Point(0, 0), parent.getSize());
          subtractInsets(parent, r);
          c.setBounds(r);
          break;
        }
      }
    }
  }

  private void subtractInsets(Container parent, Rectangle r) {
    Insets i = parent.getInsets();
    if (i != null) {
      r.x += i.left;
      r.y += i.top;
      r.width -= i.left + i.right;
      r.height -= i.top + i.bottom;
    }
  }

}
