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
package org.eclipse.scout.rt.ui.swt;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.ILayoutExtension;

/**
 * Dynamic layout using logical grid data {@link LogicalGridData} to arrange
 * fields. The grid data per field can be passed when adding the component to
 * the container or set as client property with name {@link LogicalGridData#CLIENT_PROPERTY_NAME}.
 */
public class LogicalGridLayout extends Layout implements ILayoutExtension {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogicalGridLayout.class);

  public static final int MIN = 0;
  public static final int PREF = 1;
  public static final int MAX = 2;

  public static final float EPS = 1E-6f;

  private boolean m_debug;
  private int m_hgap;
  private int m_vgap;
  private LogicalGridLayoutInfo m_info;
  private CompositeObject m_infoCacheKey;

  public LogicalGridLayout(int hgap, int vgap) {
    m_hgap = hgap;
    m_vgap = vgap;
  }

  @Override
  public int computeMinimumWidth(Composite parent, boolean changed) {
    return computeMinimumSize(parent, changed).x;
  }

  public Point computeMinimumSize(Composite parent, boolean changed) {
    return computeSize(parent, changed, SWT.DEFAULT, SWT.DEFAULT, MIN);
  }

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    return computeSize(composite, flushCache, wHint, hHint, PREF);
  }

  @Override
  public int computeMaximumWidth(Composite parent, boolean changed) {
    return computeSize(parent, changed, SWT.DEFAULT, SWT.DEFAULT, MAX).x;
  }

  public Point computeSize(Composite composite, boolean changed, int wHint, int hHint, int sizeFlag) {
    if (changed) {
      m_info = null;
    }
    if (wHint <= 0) {
      wHint = SWT.DEFAULT;
    }
    validateLayout(composite, wHint, changed);

    Point min = new Point(0, 0);
    Point pref = new Point(0, 0);
    Point max = new Point(0, 0);
    // w
    int useCount = 0;
    for (int i = 0; i < m_info.cols; i++) {
      if (useCount > 0) {
        min.x = min.x + m_hgap;
        pref.x = pref.x + m_hgap;
        max.x = max.x + m_hgap;
      }
      min.x = min.x + m_info.width[i][MIN];
      pref.x = pref.x + m_info.width[i][PREF];
      max.x = max.x + m_info.width[i][MAX];
      useCount++;
    }
    // h
    useCount = 0;
    for (int i = 0; i < m_info.rows; i++) {
      if (useCount > 0) {
        min.y = min.y + m_vgap;
        pref.y = pref.y + m_vgap;
        max.y = max.y + m_vgap;
      }
      min.y = min.y + m_info.height[i][MIN];
      pref.y = pref.y + m_info.height[i][PREF];
      max.y = max.y + m_info.height[i][MAX];
      useCount++;
    }
    Point size = new Point(0, 0);
    switch (sizeFlag) {
      case MIN:
        size.x = min.x;
        size.y = min.y;
        break;
      case MAX:
        size.x = min.x;
        size.y = min.y;
        break;
      default:
        // adjust width
        if (wHint == SWT.DEFAULT) {
          size.x = pref.x;
        }
        else {
          size.x = wHint;
          size.x = Math.min(max.x, size.x);
          size.x = Math.max(min.x, size.x);
        }
        // adjust heigth
        if (hHint == SWT.DEFAULT) {
          size.y = pref.y;
        }
        else {
          size.y = hHint;
          size.y = Math.min(max.y, size.y);
          size.y = Math.max(min.y, size.y);
        }
        break;
    }
    return size;
  }

  @Override
  protected void layout(Composite parent, boolean flushCache) {
    if (flushCache) {
      m_info = null;
    }
    validateLayout(parent, parent.getSize().x, flushCache);
    Rectangle clientArea = parent.getClientArea();
    Point size = new Point(clientArea.width, clientArea.height);
    Rectangle[][] cellBounds = m_info.layoutCellBounds(size);
    if (m_debug || LOG.isDebugEnabled()) {
      dumpLayoutInfo(parent);
    }
    // bounds
    int n = m_info.components.length;
    for (int i = 0; i < n; i++) {
      Control comp = m_info.components[i];
      LogicalGridData data = m_info.gridDatas[i];
      Rectangle r1 = cellBounds[data.gridy][data.gridx];
      Rectangle r2 = cellBounds[data.gridy + data.gridh - 1][data.gridx + data.gridw - 1];
      Rectangle r = r1.union(r2);
      if (data.topInset > 0) {
        r.y += data.topInset;
        r.height -= data.topInset;
      }
      if (data.fillHorizontal && data.fillVertical) {
        // ok
      }
      else {
        Point d = new Point(m_info.componentWidths[i], m_info.componentHeights[i]);
        if (!data.fillHorizontal) {
          if (d.x < r.width) {
            int delta = r.width - d.x;
            r.width = d.x;
            if (data.horizontalAlignment == 0) {
              r.x += delta / 2;
            }
            else if (data.horizontalAlignment > 0) {
              r.x += delta;
            }
          }
        }
        if (!data.fillVertical) {
          if (d.y < r.height) {
            int delta = r.height - d.y;
            if (data.heightHint == 0) {
              r.height = d.y;
            }
            else {
              r.height = data.heightHint;
            }
            if (data.verticalAlignment == 0) {
              r.y += delta / 2;
            }
            else if (data.verticalAlignment > 0) {
              r.y += delta;
            }
          }
        }
      }
      comp.setBounds(r);
    }

  }

  public void setDebug(boolean b) {
    m_debug = b;
  }

  public void dumpLayoutInfo(Composite parent) {
    dumpLayoutInfo(parent, new PrintWriter(System.out));
  }

  public void dumpLayoutInfo(Composite parent, PrintWriter out) {
    Point parentSize = parent.getSize();
    Rectangle[][] cellBounds = m_info.layoutCellBounds(parentSize);
    Object field = SwtScoutComposite.getScoutModelOnWidget(parent);
    String className = "undefined (PROP_SCOUT_OBJECT not set!)";
    if (field != null) {
      className = field.getClass().getSimpleName();
    }
    out.println("DUMP layout of: " + className + " compSize= " + parentSize);
    out.println("  containerBounds = " + parent.getClientArea());
    out.println("  Fields ---");
    for (int i = 0; i < m_info.components.length; i++) {
      Control c = m_info.components[i];
      LogicalGridData data = m_info.gridDatas[i];
      try {
        Rectangle r1 = cellBounds[data.gridy][data.gridx];
        Rectangle r2 = cellBounds[data.gridy + data.gridh - 1][data.gridx + data.gridw - 1];
        Rectangle r = r1.union(r2);
        String scoutObjectName = "NOT DEFINED";
        Object scoutObject = SwtScoutComposite.getScoutModelOnWidget(c);
        if (scoutObject != null) {
          scoutObjectName = scoutObject.getClass().getSimpleName();
        }
        out.println("   b=" + r + " " + scoutObjectName);
        if (scoutObject instanceof IFormField) {
          out.println("   model grid: " + ((IFormField) scoutObject).getGridData().toString());
        }
        out.println("   uiGridData: " + data.toString());
      }
      catch (ArrayIndexOutOfBoundsException e) {
        out.print("unable to print layout info of: " + data);
        e.printStackTrace();
      }
    }
    out.flush();
  }

  /**
   * @deprecated use {@link #validateLayout(Composite, int, boolean)}
   */
  @Deprecated
  protected void validateLayout(Composite parent) {
    validateLayout(parent, SWT.DEFAULT, false);
  }

  protected void validateLayout(Composite parent, int wHint, boolean flushCache) {
    CompositeObject newKey = new CompositeObject(parent.getSize(), wHint == SWT.DEFAULT ? (m_infoCacheKey != null ? m_infoCacheKey.getComponent(1) : wHint) : wHint);
    //check cache key
    if (m_info != null) {
      if (m_infoCacheKey == null || !m_infoCacheKey.equals(newKey)) {
        m_info = null;
        m_infoCacheKey = null;
      }
    }
    if (m_info != null && m_infoCacheKey != null) {
      return;
    }
    ArrayList<Control> visibleComps = new ArrayList<Control>();
    ArrayList<LogicalGridData> visibleCons = new ArrayList<LogicalGridData>();
    for (Control comp : parent.getChildren()) {
      if (comp.getVisible() && comp.getLayoutData() instanceof LogicalGridData) {
        visibleComps.add(comp);
        LogicalGridData cons = (LogicalGridData) comp.getLayoutData();
        cons.validate();
        visibleCons.add(cons);
      }
    }
    m_info = new LogicalGridLayoutInfo(visibleComps.toArray(new Control[visibleComps.size()]), visibleCons.toArray(new LogicalGridData[visibleCons.size()]), m_hgap, m_vgap, wHint, flushCache);
    m_infoCacheKey = newKey;
  }

}
