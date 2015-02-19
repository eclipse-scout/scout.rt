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
package org.eclipse.scout.rt.ui.swing.ext.internal;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author Andreas Hoegger
 * @since 4.0.0
 */
public class GridLayoutCanvas extends JPanel {
  private static final long serialVersionUID = 1L;
  public final List<GridLayoutCanvas.Line> m_lines = new ArrayList<GridLayoutCanvas.Line>();
  public final List<GridLayoutCanvas.Bounds> m_bounds = new ArrayList<GridLayoutCanvas.Bounds>();

  public GridLayoutCanvas() {
    setOpaque(false);

  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
//
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setStroke(new BasicStroke(2));
    g2d.setComposite(AlphaComposite.SrcOver.derive(0.35f));
    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, getWidth(), getHeight());

    // paint lines and borders
    for (Bounds b : m_bounds) {
      Color bForegroud = g2d.getColor();
      try {
        if (b.color != null) {
          g2d.setColor(b.color);
        }
        g2d.drawRect(b.bounds.x, b.bounds.y, b.bounds.width, b.bounds.height);
      }
      finally {
        g2d.setColor(bForegroud);
      }
    }
    for (Line l : m_lines) {
      Color bForegroud = g2d.getColor();
      try {
        if (l.color != null) {
          g2d.setColor(l.color);
        }
        g2d.drawLine(l.start.x, l.start.y, l.end.x, l.end.y);
      }
      finally {
        g2d.setColor(bForegroud);
      }
    }
  }

  public void addBounds(Bounds bounds) {
    m_bounds.add(bounds);
    repaint();
  }

  public void addLine(Line line) {
    m_lines.add(line);
    repaint();
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
