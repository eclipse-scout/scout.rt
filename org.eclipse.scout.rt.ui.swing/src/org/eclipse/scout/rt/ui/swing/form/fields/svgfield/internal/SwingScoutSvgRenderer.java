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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.eclipse.scout.rt.shared.data.form.fields.svgfield.CircleElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ImageElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.PointElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.PolygonElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.RectangleElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ResourceElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.TextElement;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 * Renderer for scout svg {@link ScoutSVGModel} pictures
 */
public class SwingScoutSvgRenderer implements ISvgRenderer {
  private final ScoutSVGModel m_svg;
  private final JComponent m_owner;
  private final ISwingEnvironment m_env;
  //
  private List<ISvgElementRenderer> m_renderers;
  private double m_maxWidht;
  private double m_maxHeight;
  //paint context
  private double m_scalingFactor = 1.0;

  public SwingScoutSvgRenderer(ScoutSVGModel svg, JComponent owner, ISwingEnvironment env) {
    m_svg = svg;
    m_owner = owner;
    m_env = env;
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
  public void paint(Graphics g, int offsetX, int offsetY, double scalingFactor) {
    m_scalingFactor = scalingFactor;
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    //setup transforms
    AffineTransform plainTx = new AffineTransform(g2.getTransform());
    plainTx.translate(offsetX, offsetY);
    AffineTransform scaledTx = new AffineTransform(plainTx);
    scaledTx.scale(m_scalingFactor, m_scalingFactor);
    for (ISvgElementRenderer elementRenderer : m_renderers) {
      elementRenderer.paint(g2, plainTx, scaledTx);
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
  private Color parseColor(String spec) {
    if (spec == null) {
      return null;
    }
    if (spec.length() == 0) {
      return null;
    }
    if (spec.length() == 8) {
      return new Color(Integer.parseInt(spec, 16), true);
    }
    return new Color(Integer.parseInt(spec, 16), false);
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

  private Font parseFont(Integer fontSize, String fontWeight) {
    if (fontSize == null && fontWeight == null) {
      return m_owner.getFont();
    }
    Font font = m_owner.getFont();
    int newSize = font.getSize();
    int newStyle = font.getStyle();
    if (fontSize != null) {
      newSize = fontSize.intValue();
    }
    if (fontWeight != null) {
      newStyle = 0;
      String low = fontWeight.toLowerCase();
      if (low.indexOf("bold") >= 0) {
        newStyle = newStyle | Font.BOLD;
      }
      if (low.indexOf("italic") >= 0) {
        newStyle = newStyle | Font.ITALIC;
      }
    }
    return new Font(font.getName(), newStyle, newSize);
  }

  /*
   * Renderers
   */

  private class CircleRenderer implements ISvgElementRenderer {
    private final CircleElement m_elem;
    private Color m_fillColor;
    private Color m_lineColor;

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
    public void paint(Graphics2D g, AffineTransform plainTx, AffineTransform scaledTx) {
      g.setTransform(scaledTx);
      if (m_fillColor != null) {
        g.setColor(m_fillColor);
        g.fillOval((int) m_elem.x, (int) m_elem.y, (int) m_elem.width, (int) m_elem.height);
      }
      if (m_lineColor != null) {
        g.setColor(m_lineColor);
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
  }

  private class ImageRenderer implements ISvgElementRenderer {
    private final ImageElement m_elem;
    private ImageIcon m_imgIcon;
    private double m_imgWidth;
    private double m_imgHeight;

    public ImageRenderer(ImageElement e) {
      m_elem = e;
      ResourceElement res = m_svg.getResources().get(m_elem.getId());
      if (res != null) {
        m_imgIcon = new ImageIcon(res.content);
      }
      else {
        Image img = m_env.getImage(m_elem.id);
        if (img != null) {
          m_imgIcon = new ImageIcon(img);
        }
      }
      m_imgWidth = m_elem.width > 0 ? m_elem.width : (m_imgIcon != null ? m_imgIcon.getIconWidth() : 0);
      m_imgHeight = m_elem.height > 0 ? m_elem.height : (m_imgIcon != null ? m_imgIcon.getIconHeight() : 0);
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    @Override
    public void paint(Graphics2D g, AffineTransform plainTx, AffineTransform scaledTx) {
      if (m_imgIcon == null) {
        return;
      }
      g.setTransform(plainTx);
      double scaledX = m_elem.x * getScalingFactor();
      double scaledY = m_elem.y * getScalingFactor();
      double scaledW = m_imgWidth * getScalingFactor();
      double scaledH = m_imgHeight * getScalingFactor();
      g.drawImage(m_imgIcon.getImage(), (int) scaledX, (int) scaledY, (int) scaledW, (int) scaledH, null);
    }

    @Override
    public double getRawWidth() {
      if (m_imgIcon == null) {
        return 0;
      }
      return m_elem.x + m_imgWidth;
    }

    @Override
    public double getRawHeight() {
      if (m_imgIcon == null) {
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
  }

  private class PointRenderer implements ISvgElementRenderer {
    private final PointElement m_elem;
    private Color m_color;

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
    public void paint(Graphics2D g, AffineTransform plainTx, AffineTransform scaledTx) {
      g.setTransform(scaledTx);
      if (m_color != null) {
        g.setColor(m_color);
        g.drawLine((int) m_elem.x, (int) m_elem.y, (int) m_elem.x, (int) m_elem.y);
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
  }

  private class PolygonRenderer implements ISvgElementRenderer {
    private final PolygonElement m_elem;
    private Color m_fillColor;
    private Color m_lineColor;
    private Polygon m_polygon;
    private double m_maxX;
    private double m_maxY;

    public PolygonRenderer(PolygonElement e) {
      m_elem = e;
      m_fillColor = parseColor(m_elem.color);
      m_lineColor = parseColor(m_elem.borderColor);
      //
      m_polygon = new Polygon();
      for (PointElement pe : m_elem.getPoints()) {
        double x = m_elem.x + pe.x;
        double y = m_elem.y + pe.y;
        m_polygon.addPoint((int) x, (int) y);
        m_maxX = Math.max(m_maxX, x);
        m_maxY = Math.max(m_maxY, y);
      }
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    @Override
    public void paint(Graphics2D g, AffineTransform plainTx, AffineTransform scaledTx) {
      g.setTransform(scaledTx);
      if (m_fillColor != null) {
        g.setColor(m_fillColor);
        g.fillPolygon(m_polygon);
      }
      if (m_lineColor != null) {
        g.setColor(m_lineColor);
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
      return m_polygon.contains(mx, my);
    }

  }

  private class RectangleRenderer implements ISvgElementRenderer {
    private final RectangleElement m_elem;
    private Color m_fillColor;
    private Color m_lineColor;

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
    public void paint(Graphics2D g, AffineTransform plainTx, AffineTransform scaledTx) {
      g.setTransform(scaledTx);
      if (m_fillColor != null) {
        g.setColor(m_fillColor);
        g.fillRect((int) m_elem.x, (int) m_elem.y, (int) m_elem.width, (int) m_elem.height);
      }
      if (m_lineColor != null) {
        g.setColor(m_lineColor);
        g.drawRect((int) m_elem.x, (int) m_elem.y, (int) m_elem.width, (int) m_elem.height);
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
  }

  private class TextRenderer implements ISvgElementRenderer {
    private final TextElement m_elem;
    private Color m_color;
    private Font m_font;
    private Rectangle m_textRect;
    private int m_textBaseline;

    public TextRenderer(TextElement e) {
      m_elem = e;
      m_color = parseColor(m_elem.color);
      if (m_color == null) {
        m_color = parseColor(m_elem.borderColor);
      }
      m_font = parseFont(m_elem.fontSize, m_elem.fontWeight);
      FontMetrics fm = m_owner.getFontMetrics(m_font);
      m_textRect = new Rectangle(0, -fm.getHeight(), 0, fm.getHeight());
      m_textBaseline = fm.getAscent();
      if (m_elem.text != null) {
        m_textRect.width = fm.stringWidth(m_elem.text);
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
    }

    @Override
    public IScoutSVGElement getModel() {
      return m_elem;
    }

    private boolean isScalingVeto() {
      return (m_elem.scaleUp != null && !m_elem.scaleUp.booleanValue() && getScalingFactor() > 1.0);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform plainTx, AffineTransform scaledTx) {
      if (m_color != null && m_elem.text != null) {
        g.setColor(m_color);
        g.setFont(m_font);
        if (isScalingVeto()) {
          g.setTransform(plainTx);
          double scaledX = m_elem.x * getScalingFactor();
          double scaledY = m_elem.y * getScalingFactor();
          g.drawString(m_elem.text, (int) (scaledX + m_textRect.x), (int) (scaledY + m_textRect.y + m_textBaseline));
        }
        else {
          g.setTransform(scaledTx);
          g.drawString(m_elem.text, (int) (m_elem.x + m_textRect.x), (int) (m_elem.y + m_textRect.y + m_textBaseline));
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
      return m_textRect.contains(dx, dy);
    }
  }

}
