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
package org.eclipse.scout.rt.ui.swing.form.fields.svgfield.internal;

/**
 * , Samuel Moser
 */
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;

public class SvgViewer extends JComponent {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SvgViewer.class);

  private ISvgRenderer m_svgRenderer;
  //transforms to fit image to view
  private Dimension m_sizeReference;
  private double m_scale = 1.0;
  private int m_translateX = 0;
  private int m_translateY = 0;

  private boolean m_insideValidateTreeProcess;

  public SvgViewer() {
    super();
    setFocusable(true);
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        if (elementAtViewLocation(e.getX(), e.getY(), true) != null) {
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else {
          setCursor(null);
        }
      }
    });
  }

  public void setSvgRenderer(ISvgRenderer renderer) {
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

  @Override
  @SuppressWarnings("deprecation")
  public void reshape(int x, int y, int w, int h) {
    boolean changed = (getWidth() != w || getHeight() != h);
    super.reshape(x, y, w, h);
    if (changed) {
      try {
        m_insideValidateTreeProcess = true;
        //
        fitPaintSize();
      }
      finally {
        m_insideValidateTreeProcess = false;
      }
    }
  }

  private void fitPaintSize() {
    Dimension size = getSize();
    if (m_sizeReference != null && m_sizeReference.equals(size)) {
      return;
    }
    m_sizeReference = size;
    if (m_sizeReference.width <= 0 || m_sizeReference.height <= 0) {
      return;
    }
    if (m_svgRenderer != null) {
      fitPaintSizeImpl(m_sizeReference.width, m_sizeReference.height, m_svgRenderer.getWidth(), m_svgRenderer.getHeight());
    }
    revalidateAndRepaint();
  }

  private void fitPaintSizeImpl(int viewWidth, int viewHeight, double rendererWidth, double rendererHeight) {
    // calculate the scaling factor to fit one dimension of the selection to the frame
    double wfactor = Math.max(0.001, 1.0 * viewWidth / rendererWidth);
    double hfactor = Math.max(0.001, 1.0 * viewHeight / rendererHeight);

    // choose min scale factor (only one factor for x ans y axle (aspect ratio))
    m_scale = Math.min(hfactor, wfactor);

    double emptyWidth = Math.max(0, viewWidth - m_scale * rendererWidth);
    double emptyHeight = Math.max(0, viewHeight - m_scale * rendererHeight);

    m_translateX = (int) (getAlignmentX() * emptyWidth);
    m_translateY = (int) (getAlignmentY() * emptyHeight);
  }

  public void revalidateAndRepaint() {
    if (!m_insideValidateTreeProcess) {
      revalidate();
      repaint();
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = new Dimension(0, 0);
    ISvgRenderer renderer = m_svgRenderer;
    if (renderer != null) {
      d.width = (int) renderer.getWidth();
      d.height = (int) renderer.getHeight();
    }
    return d;
  }

  @Override
  public Dimension getMinimumSize() {
    Dimension d = new Dimension(0, 0);
    return d;
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension d;
    ISvgRenderer renderer = m_svgRenderer;
    if (renderer != null) {
      d = new Dimension(0, 0);
      d.width = (int) renderer.getWidth();
      d.height = (int) renderer.getHeight();
    }
    else {
      d = getPreferredSize();
    }
    return d;
  }

  @Override
  public void paintComponent(Graphics g) {
    // paint background
    if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    ISvgRenderer renderer = m_svgRenderer;
    if (renderer == null) {
      return;
    }
    renderer.paint(g, m_translateX, m_translateY, m_scale);
  }

}
