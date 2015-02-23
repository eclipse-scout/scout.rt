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
package org.eclipse.scout.rt.ui.swt.internal.debug.layout.spy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class GridCanvas extends Canvas {

  public final List<Line> m_lines = new ArrayList<GridCanvas.Line>();
  public final List<Bounds> m_bounds = new ArrayList<GridCanvas.Bounds>();

  /**
   * @param parent
   * @param style
   */
  public GridCanvas(Composite parent) {
    super(parent, SWT.INHERIT_NONE);
    addPaintListener(new PaintListener() {

      @Override
      public void paintControl(PaintEvent e) {
        for (Bounds b : m_bounds) {
          Color bForegroud = e.gc.getForeground();
          int bAlpha = e.gc.getAlpha();
          try {
            e.gc.setAlpha(255);
            if (b.color != null) {
              e.gc.setForeground(b.color);
              e.gc.drawRectangle(b.bounds);
            }
          }
          finally {
            e.gc.setAlpha(bAlpha);
            e.gc.setForeground(bForegroud);
          }
        }
        for (Line l : m_lines) {
          Color backupForegroud = e.gc.getForeground();
          int bAlpha = e.gc.getAlpha();
          try {
            e.gc.setAlpha(255);
            if (l.color != null) {
              e.gc.setForeground(l.color);
              e.gc.drawLine(l.start.x, l.start.y, l.end.x, l.end.y);
            }
          }
          finally {
            e.gc.setAlpha(bAlpha);
            e.gc.setForeground(backupForegroud);
          }
        }
      }
    });
  }

  public void addBounds(Bounds bounds) {
    m_bounds.add(bounds);
    redraw();
  }

  public void addLine(Line line) {
    m_lines.add(line);
    redraw();
  }

  public static class Line {
    public Point start;
    public Point end;
    public Color color;

    public Line(Point start, Point end, Color color) {
      this.start = start;
      this.end = end;
      this.color = color;
    }
  }

  public static class Bounds {
    public Rectangle bounds;
    public Color color;

    public Bounds(Rectangle bounds, Color color) {
      this.bounds = bounds;
      this.color = color;
    }
  }
}
