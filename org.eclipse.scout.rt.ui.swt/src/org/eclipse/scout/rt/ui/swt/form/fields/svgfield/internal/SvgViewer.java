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
package org.eclipse.scout.rt.ui.swt.form.fields.svgfield.internal;

import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class SvgViewer extends Canvas {

  private int m_xAglin = SWT.CENTER;
  private int m_yAglin = SWT.CENTER;
  private Cursor m_handCursor;
  private ISvgRenderer m_svgRenderer;
  //transforms to fit image to view
  private Point m_sizeReference;
  private double m_scale = 1.0;
  private int m_translateX = 0;
  private int m_translateY = 0;

  public SvgViewer(Composite parent) {
    super(parent, SWT.NONE);
    m_handCursor = new Cursor(this.getDisplay(), SWT.CURSOR_HAND);
    setLayout(new Layout() {
      @Override
      protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        Point size = new Point(0, 0);
        ISvgRenderer renderer = m_svgRenderer;
        if (renderer != null) {
          size.x = (int) renderer.getWidth();
          size.y = (int) renderer.getHeight();
        }
        return size;
      }

      @Override
      protected void layout(Composite composite, boolean flushCache) {
        fitPaintSize();
      }
    });
    addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent e) {
        handleSwtPaintEvent(e.gc);
      }
    });
    addMouseMoveListener(new MouseMoveListener() {
      @Override
      public void mouseMove(MouseEvent e) {
        if (elementAtViewLocation(e.x, e.y, true) != null) {
          setCursor(m_handCursor);
        }
        else {
          setCursor(null);
        }
      }
    });
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
  }

  private void freeResources() {
    if (m_handCursor != null && !m_handCursor.isDisposed()) {
      m_handCursor.dispose();
      m_handCursor = null;
    }
  }

  public void setSvgRenderer(ISvgRenderer renderer) {
    if (m_svgRenderer != null) {
      m_svgRenderer.dispose();
    }
    m_svgRenderer = renderer;
    if (m_svgRenderer != null) {
      m_sizeReference = null;
      fitPaintSize();
    }
  }

  public ISvgRenderer getSvgRenderer() {
    return m_svgRenderer;
  }

  /**
   * @return the element at the location in view (mouse) coordinates
   */
  public IScoutSVGElement elementAtViewLocation(int viewX, int viewY, Boolean interactive) {
    if (m_svgRenderer == null) {
      return null;
    }
    double mx = viewX - m_translateX;
    double my = viewY - m_translateY;
    mx = mx / m_scale;
    my = my / m_scale;
    return m_svgRenderer.elementAtModelLocation(mx, my, interactive);
  }

  private void fitPaintSize() {
    Point size = getSize();
    if (m_sizeReference != null && m_sizeReference.equals(size)) {
      return;
    }
    m_sizeReference = size;
    if (m_sizeReference.x <= 0 || m_sizeReference.y <= 0) {
      return;
    }
    if (m_svgRenderer != null) {
      fitPaintSizeImpl(m_sizeReference.x, m_sizeReference.y, m_svgRenderer.getWidth(), m_svgRenderer.getHeight());
    }
    redraw();
  }

  private void fitPaintSizeImpl(int viewWidth, int viewHeight, double rendererWidth, double rendererHeight) {
    // calculate the scaling factor to fit one dimension of the selection to the frame
    double wfactor = Math.max(0.001, 1.0 * viewWidth / rendererWidth);
    double hfactor = Math.max(0.001, 1.0 * viewHeight / rendererHeight);

    // choose min scale factor (only one factor for x ans y axle (aspect ratio))
    m_scale = Math.min(hfactor, wfactor);

    double emptyWidth = Math.max(0, viewWidth - m_scale * rendererWidth);
    double emptyHeight = Math.max(0, viewHeight - m_scale * rendererHeight);

    double alignX = 0;
    switch (getAlignmentX()) {
      case SWT.CENTER:
        alignX = 0.5;
        break;
      case SWT.RIGHT:
        alignX = 1.0;
        break;
    }
    double alignY = 0;
    switch (getAlignmentY()) {
      case SWT.CENTER:
        alignY = 0.5;
        break;
      case SWT.RIGHT:
        alignY = 1.0;
        break;
    }

    m_translateX = (int) (alignX * emptyWidth);
    m_translateY = (int) (alignY * emptyHeight);
  }

  private void handleSwtPaintEvent(GC gc) {
    ISvgRenderer renderer = m_svgRenderer;
    if (renderer == null) {
      return;
    }
    gc.setAntialias(SWT.ON);
    gc.setInterpolation(SWT.HIGH);
    renderer.paint(gc, m_translateX, m_translateY, m_scale);
  }

  public void setAlignmentX(int alignment) {
    m_xAglin = alignment;
  }

  public int getAlignmentX() {
    return m_xAglin;
  }

  public void setAlignmentY(int alignment) {
    m_yAglin = alignment;
  }

  public int getAlignmentY() {
    return m_yAglin;
  }

}
