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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.CircleElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ImageElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.PointElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.PolygonElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.RectangleElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ResourceElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.TextElement;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Control;

/**
 * Renderer for scout svg {@link ScoutSVGModel} pictures
 */
public class SwtScoutSvgRenderer implements ISvgRenderer {
  private final ScoutSVGModel m_svg;
  private final Control m_owner;
  private final ISwtEnvironment m_env;
  private Transform m_plainTx;
  private Transform m_scaledTx;
  //
  private List<ISvgElementRenderer> m_renderers;
  private double m_maxWidht;
  private double m_maxHeight;
  //paint context
  private double m_scalingFactor = 1.0;

  public SwtScoutSvgRenderer(ScoutSVGModel svg, Control owner, ISwtEnvironment env) {
    m_svg = svg;
    m_owner = owner;
    m_env = env;
    m_plainTx = new Transform(owner.getDisplay());
    m_scaledTx = new Transform(owner.getDisplay());
    owner.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        dispose();
      }
    });
    //
    m_renderers = new ArrayList<ISvgElementRenderer>();
    for (IScoutSVGElement e : m_svg.getGraphicsElements()) {
      ISvgElementRenderer er = createRenderer(e);
      if (er != null) {
        m_renderers.add(er);
        m_maxWidht = Math.max(m_maxWidht, er.getRawWidth());
        m_maxHeight = Math.max(m_maxHeight, er.getRawHeight());
      }
    }
  }

  @Override
  public void dispose() {
    freeResources();
  }

  private void freeResources() {
    if (m_plainTx != null && !m_plainTx.isDisposed()) {
      m_plainTx.dispose();
      m_plainTx = null;
    }
    if (m_scaledTx != null && !m_scaledTx.isDisposed()) {
      m_scaledTx.dispose();
      m_scaledTx = null;
    }
    for (ISvgElementRenderer r : m_renderers) {
      r.dispose();
    }
    m_renderers.clear();
  }

  protected ISvgElementRenderer createRenderer(IScoutSVGElement graphicalElement) {
    if (graphicalElement instanceof CircleElement) {
      return new CircleRenderer((CircleElement) graphicalElement);
    }
    if (graphicalElement instanceof ImageElement) {
      return new ImageRenderer((ImageElement) graphicalElement);
    }
    if (graphicalElement instanceof PointElement) {
      return new PointRenderer((PointElement) graphicalElement);
    }
    if (graphicalElement instanceof PolygonElement) {
      return new PolygonRenderer((PolygonElement) graphicalElement);
    }
    if (graphicalElement instanceof RectangleElement) {
      return new RectangleRenderer((RectangleElement) graphicalElement);
    }
    if (graphicalElement instanceof TextElement) {
      return new TextRenderer((TextElement) graphicalElement);
    }
    return null;
  }

  public double getScalingFactor() {
    return m_scalingFactor;
  }

  @Override
  public IScoutSVGElement elementAtModelLocation(double mx, double my, Boolean interactive) {
    //reverse order
    int n = m_renderers.size();
    for (int i = n - 1; i >= 0; i--) {
      ISvgElementRenderer elementRenderer = m_renderers.get(i);
      if (interactive != null && interactive.booleanValue() != elementRenderer.getModel().isInteractive()) {
        continue;
      }
      if (elementRenderer.contains(mx, my)) {
        return elementRenderer.getModel();
      }
    }
    return null;
  }

  @Override
  public void paint(GC g, int offsetX, int offsetY, double scalingFactor) {
    m_scalingFactor = scalingFactor;
    //setup transforms
    m_plainTx.identity();
    m_plainTx.translate(offsetX, offsetY);
    m_scaledTx.identity();
    m_scaledTx.translate(offsetX, offsetY);
    m_scaledTx.scale((float) scalingFactor, (float) scalingFactor);
    for (ISvgElementRenderer elementRenderer : m_renderers) {
      elementRenderer.paint(g, m_plainTx, m_scaledTx);
    }
  }

  @Override
  public double getWidth() {
    return m_maxWidht;
  }

  @Override
  public double getHeight() {
    return m_maxHeight;
  }

  /**
   * <pre>
   * null
   * ""
   * "aarrggbb"
   * "rrggbb"
   * </pre>
   */
  private ColorWithAlpha parseColor(String spec) {
    if (spec == null) {
      return null;
    }
    if (spec.length() == 0) {
      return null;
    }
    ColorWithAlpha c = new ColorWithAlpha();
    int i = Integer.parseInt(spec, 16);
    if (spec.length() == 8) {
      c.a = (i >> 24) & 0xff;
    }
    c.rgb = m_env.getColor(new RGB((i >> 16) & 0xff, (i >> 8) & 0xff, (i) & 0xff));
    return c;
  }

  private int parseAlign(String alignSpec) {
    if (alignSpec == null) {
      return -1;
    }
    alignSpec = alignSpec.toLowerCase();
    if (alignSpec.equals("center")) return 0;
    if (alignSpec.equals("right")) return 1;
    return -1;
  }

  private Integer parseFontStyle(String fontWeight) {
    if (fontWeight == null) {
      return null;
    }
    int newStyle = 0;
    String low = fontWeight.toLowerCase();
    if (low.indexOf("bold") >= 0) {
      newStyle = newStyle | FontSpec.STYLE_BOLD;
    }
    if (low.indexOf("italic") >= 0) {
      newStyle = newStyle | FontSpec.STYLE_ITALIC;
    }
    return newStyle;
  }

  private void setBackgroundColor(GC g, ColorWithAlpha c) {
    g.setBackground(c.rgb);
    g.setAlpha(c.a);
  }

  private void setForegroundColor(GC g, ColorWithAlpha c) {
    g.setForeground(c.rgb);
    g.setAlpha(c.a);
  }

  /*
   * Renderers
   */

  private class CircleRenderer implements ISvgElementRenderer {
    private final CircleElement m_elem;
    private ColorWithAlpha m_fillColor;
    private ColorWithAlpha m_lineColor;

    public CircleRenderer(CircleElement e) {
      m_elem = e;
      m_fillColor = parseColor(m_elem.color);
      m_lineColor = parseColor(m_elem.borderColor);
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    @Override
    public void paint(GC g, Transform plainTx, Transform scaledTx) {
      g.setTransform(scaledTx);
      if (m_fillColor != null) {
        setBackgroundColor(g, m_fillColor);
        g.fillOval((int) m_elem.x, (int) m_elem.y, (int) m_elem.width, (int) m_elem.height);
      }
      if (m_lineColor != null) {
        setForegroundColor(g, m_lineColor);
        g.drawOval((int) m_elem.x, (int) m_elem.y, (int) m_elem.width, (int) m_elem.height);
      }
    }

    @Override
    public double getRawWidth() {
      return m_elem.x + m_elem.width;
    }

    @Override
    public double getRawHeight() {
      return m_elem.y + m_elem.height;
    }

    @Override
    public boolean contains(double mx, double my) {
      double xr1 = (mx - m_elem.x - m_elem.width / 2) / m_elem.width * 2.0;
      double yr2 = (my - m_elem.y - m_elem.height / 2) / m_elem.height * 2.0;
      return xr1 * xr1 + yr2 * yr2 <= 1.0;
    }

    @Override
    public void dispose() {
    }
  }

  private class ImageRenderer implements ISvgElementRenderer {
    private final ImageElement m_elem;
    private Image m_img;
    private double m_imgWidth;
    private double m_imgHeight;

    public ImageRenderer(ImageElement e) {
      m_elem = e;
      ResourceElement res = m_svg.getResources().get(m_elem.getId());
      if (res != null) {
        m_img = new Image(m_owner.getDisplay(), new ByteArrayInputStream(res.content));
      }
      else {
        m_img = new Image(m_owner.getDisplay(), m_env.getIcon(m_elem.id), SWT.IMAGE_COPY);//TODO put to cache
      }
      m_imgWidth = m_elem.width > 0 ? m_elem.width : (m_img != null ? m_img.getImageData().width : 0);
      m_imgHeight = m_elem.height > 0 ? m_elem.height : (m_img != null ? m_img.getImageData().height : 0);
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    @Override
    public void paint(GC g, Transform plainTx, Transform scaledTx) {
      if (m_img == null) {
        return;
      }
      g.setTransform(plainTx);
      double scaledX = m_elem.x * getScalingFactor();
      double scaledY = m_elem.y * getScalingFactor();
      double scaledW = m_imgWidth * getScalingFactor();
      double scaledH = m_imgHeight * getScalingFactor();
      g.drawImage(m_img, 0, 0, m_img.getImageData().width, m_img.getImageData().height, (int) scaledX, (int) scaledY, (int) scaledW, (int) scaledH);
    }

    @Override
    public double getRawWidth() {
      if (m_img == null) {
        return 0;
      }
      return m_elem.x + m_imgWidth;
    }

    @Override
    public double getRawHeight() {
      if (m_img == null) {
        return 0;
      }
      return m_elem.y + m_imgHeight;
    }

    @Override
    public boolean contains(double mx, double my) {
      double dx = mx - m_elem.x;
      double dy = my - m_elem.y;
      return 0 <= dx && dx <= m_imgWidth && 0 <= dy && dy <= m_imgHeight;
    }

    @Override
    public void dispose() {
      if (m_img != null && !m_img.isDisposed()) {
        m_img.dispose();
        m_img = null;
      }
    }
  }

  private class PointRenderer implements ISvgElementRenderer {
    private final PointElement m_elem;
    private ColorWithAlpha m_color;

    public PointRenderer(PointElement e) {
      m_elem = e;
      m_color = parseColor(m_elem.color);
      if (m_color == null) {
        m_color = parseColor(m_elem.borderColor);
      }
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    @Override
    public void paint(GC g, Transform plainTx, Transform scaledTx) {
      g.setTransform(scaledTx);
      if (m_color != null) {
        setBackgroundColor(g, m_color);
        g.fillRectangle((int) m_elem.x, (int) m_elem.y, 1, 1);
      }
    }

    @Override
    public double getRawWidth() {
      return m_elem.x + 1;
    }

    @Override
    public double getRawHeight() {
      return m_elem.y + 1;
    }

    @Override
    public boolean contains(double mx, double my) {
      double dx = mx - m_elem.x;
      double dy = my - m_elem.y;
      return Math.abs(dx) <= 0.5 && Math.abs(dy) <= 0.5;
    }

    @Override
    public void dispose() {
    }
  }

  private class PolygonRenderer implements ISvgElementRenderer {
    private final PolygonElement m_elem;
    private ColorWithAlpha m_fillColor;
    private ColorWithAlpha m_lineColor;
    private int[] m_polygon;
    private double m_maxX;
    private double m_maxY;

    public PolygonRenderer(PolygonElement e) {
      m_elem = e;
      m_fillColor = parseColor(m_elem.color);
      m_lineColor = parseColor(m_elem.borderColor);
      //
      m_polygon = new int[m_elem.getPoints().size() * 2];
      int index = 0;
      for (PointElement pe : m_elem.getPoints()) {
        double x = m_elem.x + pe.x;
        double y = m_elem.y + pe.y;
        m_polygon[index++] = (int) x;
        m_polygon[index++] = (int) y;
        m_maxX = Math.max(m_maxX, x);
        m_maxY = Math.max(m_maxY, y);
      }
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    @Override
    public void paint(GC g, Transform plainTx, Transform scaledTx) {
      g.setTransform(scaledTx);
      if (m_fillColor != null) {
        setBackgroundColor(g, m_fillColor);
        g.fillPolygon(m_polygon);
      }
      if (m_lineColor != null) {
        setForegroundColor(g, m_lineColor);
        g.drawPolygon(m_polygon);
      }
    }

    @Override
    public double getRawWidth() {
      return m_maxX + 1;
    }

    @Override
    public double getRawHeight() {
      return m_maxY + 1;
    }

    @Override
    public boolean contains(double mx, double my) {
      return polygonContainsImpl(m_polygon, mx, my);
    }

    @Override
    public void dispose() {
    }

    private boolean polygonContainsImpl(int[] xy, double x, double y) {
      int pointCount = xy.length / 2;
      if (pointCount <= 2) {
        return false;
      }
      int hitCount = 0;
      int lastX = xy[(pointCount - 1) * 2];
      int lastY = xy[(pointCount - 1) * 2 + 1];
      int curX;
      int curY;
      for (int i = 0; i < pointCount; lastX = curX, lastY = curY, i++) {
        curX = xy[(i) * 2];
        curY = xy[(i) * 2 + 1];
        if (curY == lastY) {
          continue;
        }
        int prevX;
        if (curX < lastX) {
          if (x >= lastX) {
            continue;
          }
          prevX = curX;
        }
        else {
          if (x >= curX) {
            continue;
          }
          prevX = lastX;
        }
        double textX, testY;
        if (curY < lastY) {
          if (y < curY || y >= lastY) {
            continue;
          }
          if (x < prevX) {
            hitCount++;
            continue;
          }
          textX = x - curX;
          testY = y - curY;
        }
        else {
          if (y < lastY || y >= curY) {
            continue;
          }
          if (x < prevX) {
            hitCount++;
            continue;
          }
          textX = x - lastX;
          testY = y - lastY;
        }

        if (textX < (testY / (lastY - curY) * (lastX - curX))) {
          hitCount++;
        }
      }
      //check even/odd
      return (hitCount & 1) != 0;
    }
  }

  private class RectangleRenderer implements ISvgElementRenderer {
    private final RectangleElement m_elem;
    private ColorWithAlpha m_fillColor;
    private ColorWithAlpha m_lineColor;

    public RectangleRenderer(RectangleElement e) {
      m_elem = e;
      m_fillColor = parseColor(m_elem.color);
      m_lineColor = parseColor(m_elem.borderColor);
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    @Override
    public void paint(GC g, Transform plainTx, Transform scaledTx) {
      g.setTransform(scaledTx);
      if (m_fillColor != null) {
        setBackgroundColor(g, m_fillColor);
        g.fillRectangle((int) m_elem.x, (int) m_elem.y, (int) m_elem.width, (int) m_elem.height);
      }
      if (m_lineColor != null) {
        setForegroundColor(g, m_lineColor);
        g.drawRectangle((int) m_elem.x, (int) m_elem.y, (int) m_elem.width, (int) m_elem.height);
      }
    }

    @Override
    public double getRawWidth() {
      return m_elem.x + m_elem.width;
    }

    @Override
    public double getRawHeight() {
      return m_elem.y + m_elem.height;
    }

    @Override
    public boolean contains(double mx, double my) {
      double dx = mx - m_elem.x;
      double dy = my - m_elem.y;
      return 0 <= dx && dx <= m_elem.width && 0 <= dy && dy <= m_elem.height;
    }

    @Override
    public void dispose() {
    }
  }

  private class TextRenderer implements ISvgElementRenderer {
    private final TextElement m_elem;
    private ColorWithAlpha m_color;
    private Font m_font;
    private Rectangle m_textRect;
    private int m_textBaseline;

    public TextRenderer(TextElement e) {
      m_elem = e;
      m_color = parseColor(m_elem.color);
      if (m_color == null) {
        m_color = parseColor(m_elem.borderColor);
      }
      m_font = m_env.getFont(m_owner.getFont(), null, parseFontStyle(m_elem.fontWeight), m_elem.fontSize);
      GC gc = new GC(m_owner);
      gc.setFont(m_font);
      m_textRect = new Rectangle(0, 0, 0, 0);
      m_textBaseline = 0;
      if (m_elem.text != null) {
        Point ext = gc.stringExtent(m_elem.text);
        m_textRect.width = ext.x;
        m_textRect.y = -ext.y;
        m_textRect.height = ext.y;
        switch (parseAlign(m_elem.align)) {
          case 0: {
            m_textRect.x -= m_textRect.width / 2;
            break;
          }
          case 1: {
            m_textRect.x -= m_textRect.width;
            break;
          }
        }
      }
      gc.dispose();
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    private boolean isScalingVeto() {
      return (m_elem.scaleUp != null && !m_elem.scaleUp.booleanValue() && getScalingFactor() > 1.0);
    }

    @Override
    public void paint(GC g, Transform plainTx, Transform scaledTx) {
      if (m_color != null && m_elem.text != null) {
        setForegroundColor(g, m_color);
        if (isScalingVeto()) {
          double scaledX = m_elem.x * getScalingFactor();
          double scaledY = m_elem.y * getScalingFactor();
          g.setFont(m_font);
          g.setTransform(plainTx);
          g.drawString(m_elem.text, (int) (scaledX + m_textRect.x), (int) (scaledY + m_textRect.y + m_textBaseline), true);
        }
        else {
          g.setFont(m_font);
          g.setTransform(scaledTx);
          g.drawString(m_elem.text, (int) (m_elem.x + m_textRect.x), (int) (m_elem.y + m_textRect.y + m_textBaseline), true);
        }
      }
    }

    @Override
    public double getRawWidth() {
      return Math.max(0, m_elem.x + m_textRect.x + m_textRect.width);
    }

    @Override
    public double getRawHeight() {
      return Math.max(0, m_elem.y + m_textRect.y + m_textRect.height);
    }

    @Override
    public boolean contains(double mx, double my) {
      double dx = mx - m_elem.x;
      double dy = my - m_elem.y;
      return m_textRect.contains((int) dx, (int) dy);
    }

    @Override
    public void dispose() {
    }
  }

}
