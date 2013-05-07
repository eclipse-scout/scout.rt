/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.eclipse.scout.rt.ui.swing.ext.activitymap.JActivityMapHeaderValidator.ColumnType;

/**
 * activity map with column model and data model
 */
public class JActivityMapHeader extends JComponent {
  private static final long serialVersionUID = 1L;

  private JActivityMap m_map;
  private JActivityMapHeaderValidator m_validator;
  // cache
  private Map<Rectangle, String> m_tooltipMapCache;

  protected JActivityMapHeader(JActivityMap map) {
    m_map = map;
    m_validator = new JActivityMapHeaderValidator();
    setBackground(Color.white);
    setToolTipText("...");// in order to easyly attach to tooltip manager
  }

  @Override
  public String getToolTipText(MouseEvent e) {
    for (Map.Entry<Rectangle, String> entry : getTooltipMapCache().entrySet()) {
      if (entry.getKey().contains(e.getPoint())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private int getHeightFromModel() {
    ActivityMapModel model = m_map.getModel();
    return model != null ? model.getHeaderHeight() : 24;
  }

  public Rectangle getMajorHeaderRect(Object majorColumn) {
    Rectangle r = m_map.getRect(m_map.getColumnModel().getMajorColumnRange(majorColumn));
    r.y = 0;
    r.height = getHeight() / 2;
    return r;
  }

  public Rectangle getMinorHeaderRect(Object minorColumn) {
    Rectangle r = m_map.getRect(m_map.getColumnModel().getMinorColumnRange(minorColumn));
    r.y = getHeightFromModel() / 2 + 1;
    r.height = getHeightFromModel() - r.y;
    return r;
  }

  private Map<Rectangle, String> getTooltipMapCache() {
    if (m_tooltipMapCache == null) {
      m_tooltipMapCache = new HashMap<Rectangle, String>();
      ActivityMapColumnModel columnModel = m_map.getColumnModel();
      Object[] majorCols = columnModel.getMajorColumns();
      for (Object majorCol : majorCols) {
        m_tooltipMapCache.put(getMajorHeaderRect(majorCol), columnModel.getMajorColumnTooltipText(majorCol));
        for (Object minorCol : columnModel.getMinorColumns(majorCol)) {
          m_tooltipMapCache.put(getMinorHeaderRect(minorCol), columnModel.getMinorColumnTooltipText(minorCol));
        }
      }
    }
    return m_tooltipMapCache;
  }

  @Override
  public void invalidate() {
    super.invalidate();
    m_tooltipMapCache = null;
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = new Dimension(super.getPreferredSize().width, getHeightFromModel());
    return d;
  }

  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    Object[] majorCols = m_map.getColumnModel().getMajorColumns();
    paintMajorHeader(g, majorCols);
    ArrayList<Object> minorColList = new ArrayList<Object>();
    for (Object o : majorCols) {
      minorColList.addAll(Arrays.asList(m_map.getColumnModel().getMinorColumns(o)));
    }
    Object[] minorCols = minorColList.toArray();
    paintMinorHeader(g, minorCols);
  }

  private void paintMajorHeader(Graphics g, Object[] majorCols) {
    g.setColor(getForeground());
    Font f = getFont();
    HashMap<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>(f.getAttributes());
    attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
    g.setFont(new Font(attributes));
    ArrayList<Rectangle> majorRects = new ArrayList<Rectangle>();
    ArrayList<Rectangle> textRects = new ArrayList<Rectangle>();
    for (Object o : majorCols) {
      Rectangle r = getMajorHeaderRect(o);
      majorRects.add(r);
      textRects.add(r);
    }
    // determine text size
    ArrayList<String> texts = new ArrayList<String>();
    List<Rectangle> validatedRects = m_validator.calculateTextSizeRectangles(majorCols, ColumnType.MAJOR, m_map.getColumnModel(), textRects, g.getFontMetrics(), texts);

    for (int i = 0; i < textRects.size(); i++) {
      String text = texts.get(i);
      if (text != null && text.length() > 0) {
        Rectangle r = validatedRects.get(i);
        paintText(g, r, text, false, true);
      }
    }
    g.setColor(Color.lightGray);
    int h = getHeight();
    for (Rectangle r : majorRects) {
      g.drawLine(r.x, h - 2, r.x, h - 1);
      g.drawLine(r.x + r.width - 1, h - 2, r.x + r.width - 1, h - 1);
    }
  }

  private void paintMinorHeader(Graphics g, Object[] minorCols) {
    g.setColor(getForeground());
    g.setFont(getFont());
    ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
    for (Object o : minorCols) {
      rects.add(getMinorHeaderRect(o));
    }

    // determine text size
    ArrayList<String> texts = new ArrayList<String>();
    List<Rectangle> validatedRects = m_validator.calculateTextSizeRectangles(minorCols, ColumnType.MINOR, m_map.getColumnModel(), rects, g.getFontMetrics(), texts);

    for (int i = 0; i < rects.size(); i++) {
      Rectangle r = validatedRects.get(i);
      String text = texts.get(i);
      paintText(g, r, text, true, true);
    }
  }

  private void paintText(Graphics g, Rectangle r, String text, boolean hcentered, boolean vcentered) {
    Shape oldShape = g.getClip();
    if (text != null) {
      FontMetrics fm = g.getFontMetrics();
      int dx = 2;
      int dy = 2 + fm.getAscent();
      if (hcentered) {
        int width = fm.stringWidth(text);
        dx = 2 + Math.max(0, r.width - 4 - width) / 2;
      }
      if (vcentered) {
        int height = fm.getAscent();
        dy = 2 + fm.getAscent() + Math.max(0, r.height - 4 - height) / 2;
      }
      g.clipRect(r.x + dx, r.y + 2, r.width - 4, r.height - 4);
      g.drawString(text, r.x + dx, r.y + dy);
    }
    g.setClip(oldShape);
  }

  /**
   * Deprecated: Use @see{JActivityMapHeaderValidator} will be removed in scout 3.10
   * checks available space for texts When a text is null or empty, the previous
   * text gets the space
   * 
   * @param validatedRects
   *          is an out parameter
   */
  @Deprecated
  private boolean validateTextSizes(FontMetrics fm, List<String> texts, List<Rectangle> rects, List<Rectangle> validatedRects) {
    validatedRects.clear();
    validatedRects.addAll(rects);
    boolean fits = true;
    for (int i = 0; i < validatedRects.size(); i++) {
      String text = texts.get(i);
      if (text != null && text.length() > 0) {
        int k = i + 1;
        while (k < validatedRects.size() && (texts.get(k) == null || texts.get(k).length() == 0)) {
          validatedRects.get(i).width += validatedRects.get(k).width;
          validatedRects.get(k).width = 0;
          k++;
        }
        if (fm.stringWidth(text) > validatedRects.get(i).width - 4) {
          fits = false;
        }
      }
    }
    return fits;
  }

}
