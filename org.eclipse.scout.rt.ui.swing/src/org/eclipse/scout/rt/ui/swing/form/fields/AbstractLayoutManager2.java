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
package org.eclipse.scout.rt.ui.swing.form.fields;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.lang.reflect.Array;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public abstract class AbstractLayoutManager2 implements LayoutManager2 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractLayoutManager2.class);

  /*
   * do not change these indexes @see getValidatedSize
   */
  public static final float EPS = 1E-6f;
  public static final int MIN_SIZE = 0;
  public static final int PREF_SIZE = 1;
  public static final int MAX_SIZE = 2;

  private boolean m_valid;
  private Dimension m_validityBasedOnParentSize;

  public AbstractLayoutManager2() {
    m_validityBasedOnParentSize = new Dimension();
  }

  public static String dump(Object o) {
    if (o == null) {
      return "null";
    }
    else if (o.getClass().isArray()) {
      int n = Array.getLength(o);
      StringBuffer b = new StringBuffer();
      b.append("[");
      for (int i = 0; i < n; i++) {
        if (i > 0) {
          b.append(",");
        }
        b.append(dump(Array.get(o, i)));
      }
      b.append("]");
      return b.toString();
    }
    else {
      String s = o.toString();
      if (o instanceof Number) {
        s = s.replaceAll("\\.0$", "");
      }
      return s;
    }
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
  }

  @Override
  public void removeLayoutComponent(Component comp) {
  }

  @Override
  public float getLayoutAlignmentX(Container parent) {
    return 0;
  }

  @Override
  public float getLayoutAlignmentY(Container parent) {
    return 0;
  }

  @Override
  public void invalidateLayout(Container parent) {
    m_valid = false;
  }

  public void invalidateLayout() {
    m_valid = false;
  }

  protected final void verifyLayout(Container parent) {
    if ((!m_valid) || !m_validityBasedOnParentSize.equals(parent.getSize())) {
      m_validityBasedOnParentSize = parent.getSize();
      synchronized (parent.getTreeLock()) {
        validateLayout(parent);
      }
      m_valid = true;
    }
  }

  /**
   * do not get tree lock here, the lock has already been acquired
   */
  protected abstract void validateLayout(Container parent);

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      return new Dimension(getLayoutSize(parent, MIN_SIZE));
    }
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      return new Dimension(getLayoutSize(parent, PREF_SIZE));
    }
  }

  @Override
  public Dimension maximumLayoutSize(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      return new Dimension(getLayoutSize(parent, MAX_SIZE));
    }
  }

  /**
   * do not get tree lock here, the lock has already been acquired
   */
  protected abstract Dimension getLayoutSize(Container parent, int sizeflag);

  /**
   * get tree lock here, the lock has not yet been acquired
   */
  @Override
  public abstract void layoutContainer(Container parent);
}
