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
package org.eclipse.scout.rt.ui.swt.util.debug;

import java.lang.reflect.Method;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class LayoutWrapper extends Layout {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LayoutWrapper.class);

  private Layout m_wrappedLayout;
  private final String name;

  public LayoutWrapper(String name, Layout wrappedLayout) {
    this.name = name;
    m_wrappedLayout = wrappedLayout;
  }

  @Override
  protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
    try {
      Method m = m_wrappedLayout.getClass().getDeclaredMethod("computeSize", Composite.class, int.class, int.class, boolean.class);
      m.setAccessible(true);

      Object t = m.invoke(m_wrappedLayout, composite, hint, hint2, flushCache);
      if (t instanceof Point) {
        Point p = (Point) t;
        System.out.println("wrappedLayout '" + name + "' computedSize: " + p.toString() + " HINTS:[" + hint + "," + hint2 + "]");
        return p;
      }
    }
    catch (Exception e) {
      LOG.warn(null, e);
    }
    return new Point(1500, 20);
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    try {
      Method m = m_wrappedLayout.getClass().getDeclaredMethod("layout", Composite.class, boolean.class);
      m.setAccessible(true);

      m.invoke(m_wrappedLayout, composite, flushCache);
      System.out.println("layout done '" + name + "'");
      return;
    }
    catch (Exception e) {
      LOG.warn(null, e);
    }

  }
}
