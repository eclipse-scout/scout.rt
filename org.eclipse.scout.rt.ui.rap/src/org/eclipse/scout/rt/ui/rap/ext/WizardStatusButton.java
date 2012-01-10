/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class WizardStatusButton extends Canvas {
  private static final long serialVersionUID = 1L;

  private static final String ELLIPSIS = "...";
  private Font m_textFont;
  private Font m_stepFont;
  private String m_text;
  private int m_step;
  private boolean m_selected;
  private Color m_selectedBorderColor;
  private Color m_selectedBackground;
  private Color m_selectedBackgroundGradient;
  private Color m_selectedForeground;
  private Color m_background;
  private Color m_backgroundGradient;
  private Color m_foreground;

  public WizardStatusButton(Composite parent, int style) {
    super(parent, style);
    initResources();
    addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
    addPaintListener(new PaintListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void paintControl(PaintEvent e) {
        paint(e.gc);
      }
    });
  }

  protected void initResources() {
    m_selectedBorderColor = new Color(this.getDisplay(), 213, 216, 243);
    m_selectedBackground = new Color(this.getDisplay(), 94, 122, 194);
    m_selectedBackgroundGradient = new Color(this.getDisplay(), 213, 216,
        243);
    m_selectedForeground = new Color(this.getDisplay(), 255, 255, 255);
    m_background = new Color(this.getDisplay(), 219, 226, 245);
    m_backgroundGradient = new Color(this.getDisplay(), 219, 226, 245);
    m_foreground = new Color(this.getDisplay(), 0, 0, 0);
  }

  protected void freeResources() {
    if (m_selectedBorderColor != null && !m_selectedBorderColor.isDisposed() && m_selectedBorderColor.getDevice() != null) {
      m_selectedBorderColor.dispose();
      m_selectedBorderColor = null;
    }
    if (m_selectedBackground != null && !m_selectedBackground.isDisposed() && m_selectedBackground.getDevice() != null) {
      m_selectedBackground.dispose();
      m_selectedBackground = null;
    }
    if (m_selectedBackgroundGradient != null && !m_selectedBackgroundGradient.isDisposed() && m_selectedBackgroundGradient.getDevice() != null) {
      m_selectedBackgroundGradient.dispose();
      m_selectedBackgroundGradient = null;
    }
    if (m_selectedForeground != null && !m_selectedForeground.isDisposed() && m_selectedForeground.getDevice() != null) {
      m_selectedForeground.dispose();
      m_selectedForeground = null;
    }
    if (m_background != null && !m_background.isDisposed() && m_background.getDevice() != null) {
      m_background.dispose();
      m_background = null;
    }
    if (m_backgroundGradient != null && !m_backgroundGradient.isDisposed() && m_backgroundGradient.getDevice() != null) {
      m_backgroundGradient.dispose();
      m_backgroundGradient = null;
    }
    if (m_foreground != null && !m_foreground.isDisposed() && m_foreground.getDevice() != null) {
      m_foreground.dispose();
      m_foreground = null;
    }
  }

  protected void paint(GC gc) {
    // gc.setAdvanced(true);
    // gc.setFillRule(SWT.FILL_WINDING);
    // gc.setInterpolation(SWT.HIGH);

    if (m_selected) {
      paintSelectedBackground(gc);
      gc.setForeground(m_selectedForeground);
    }
    else {
      paintBackground(gc);
      gc.setForeground(m_foreground);
    }

    Rectangle bounds = getBounds();
    Point imageSize = computeSize(-1, -1, false);
    gc.setFont(m_stepFont);
    Point stepSize = gc.textExtent("" + m_step);
    gc.drawText(m_step + "", (imageSize.y - stepSize.x) / 2,
        (imageSize.y - stepSize.y) / 2, true);
    gc.setFont(m_textFont);
    String text = m_text;
    if (text == null) {
      text = "";
    }
    Point textBounds = gc.textExtent(text);
    int availableSpace = bounds.width - height - 6;
    if (availableSpace < textBounds.x) {
      setToolTipText(text);
      text = shortenText(gc, text, availableSpace);
      textBounds = gc.textExtent(text);
    }
    else {
      setToolTipText(null);
    }
    gc.drawText(text, height + 3, (height - textBounds.y) / 2, true);
  }

  int height = 26;
  int borderWith = 2;

  protected void paintSelectedBackground(GC gc) {

    Rectangle bounds = getBounds();
    // circle right

    gc.setBackground(m_selectedBorderColor);
    gc.fillOval(bounds.width - height, 0, height, height);
    gc.setBackground(m_selectedBackgroundGradient);
    gc.fillOval(bounds.width - height + borderWith, borderWith, height - 2
        * borderWith, height - 2 * borderWith);

    // border rectangle center
    gc.setBackground(m_selectedBorderColor);
    gc.fillRectangle(height / 2, 0, bounds.width - height, height);
    gc.setBackground(m_selectedBackgroundGradient);
    gc.setForeground(m_selectedBackground);
    gc.fillGradientRectangle(height / 2 + borderWith, borderWith,
        bounds.width - height - 2 * borderWith,
        height - 2 * borderWith, false);

    // border circle left
    gc.setAlpha(150);
    gc.setBackground(m_selectedBorderColor);
    gc.fillOval(0, 0, height, height);
    gc.setAlpha(255);
    gc.setBackground(m_selectedBackground);
    gc.fillOval(borderWith, borderWith, height - 2 * borderWith, height - 2
        * borderWith);
  }

  protected void paintBackground(GC gc) {

    Rectangle bounds = getBounds();
    // circle right

    gc.setBackground(m_backgroundGradient);
    gc.fillOval(bounds.width - height, 0, height, height);
    gc.setBackground(m_backgroundGradient);
    gc.fillOval(bounds.width - height + borderWith, borderWith, height - 2
        * borderWith, height - 2 * borderWith);

    // border rectangle center
    gc.setBackground(m_backgroundGradient);
    gc.fillRectangle(height / 2, 0, bounds.width - height, height);
    gc.setBackground(m_backgroundGradient);
    gc.setForeground(m_background);
    gc.fillGradientRectangle(height / 2 + borderWith, borderWith,
        bounds.width - height - 2 * borderWith,
        height - 2 * borderWith, false);

    // border circle left
    gc.setAlpha(150);
    gc.setBackground(m_backgroundGradient);
    gc.fillOval(0, 0, height, height);
    gc.setAlpha(255);
    gc.setBackground(m_background);
    gc.fillOval(borderWith, borderWith, height - 2 * borderWith, height - 2
        * borderWith);
  }

  protected String shortenText(GC gc, String t, int labelWidth) {
    if (t == null) {
      return null;
    }
    String text = t;
    String rangeWord = "";
    int textWidth = gc.textExtent(ELLIPSIS).x;
    int rangeWidth = gc.textExtent(rangeWord).x;
    // initial number of characters
    int s = text.length();
    // shorten string
    while (s >= 0) {
      String s1 = t.substring(0, s);
      int l1 = gc.textExtent(s1).x;
      if (l1 + textWidth + rangeWidth < labelWidth) {
        t = s1 + ELLIPSIS + rangeWord;
        break;
      }
      s--;
    }
    return t;
  }

  private Point m_cachedSize = new Point(0, 0);

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (true) {
      Point size = new Point(height, height);
      GC gc = null;
      try {
        gc = new GC(this);
        gc.setFont(getTextFont());
        Point stringSize = gc.stringExtent(getText());
        size.x = Math.max(wHint, stringSize.x + height + 10);
      }
      catch (Exception e) {
        if (gc != null && !gc.isDisposed() && gc.getDevice() != null) {
          gc.dispose();
        }
      }
      m_cachedSize = size;
    }
    return m_cachedSize;
  }

  public void setText(String text) {
    m_text = text;
    redraw();
  }

  public String getText() {
    return m_text;
  }

  public int getStep() {
    return m_step;
  }

  public void setStep(int step) {
    m_step = step;
    redraw();
  }

  public Font getTextFont() {
    return m_textFont;
  }

  public void setTextFont(Font textFont) {
    m_textFont = textFont;
    redraw();
  }

  public Font getStepFont() {
    return m_stepFont;
  }

  public void setStepFont(Font stepFont) {
    m_stepFont = stepFont;
    redraw();
  }

  public void setSelected(boolean selected) {
    m_selected = selected;
    redraw();
  }

  public boolean isSelected() {
    return m_selected;
  }
}
