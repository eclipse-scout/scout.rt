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
package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SnapButtonMaximized extends Canvas {

  private String m_text;
  private Image m_image;
  private boolean m_selected;

  private EventListenerList m_eventListener = new EventListenerList();
  // internal
  private boolean m_mouseHover;
  private boolean m_focussed;
  private Listener m_controlListener = new P_ControlListener();
  // spacing
  private int m_gap = 5;
  private int m_insets = 2;
  private int m_borderInsets = 3;
  private int m_borderWidth = 1;
  // resources
  private Color m_borderColor;
  private Color m_selectedBackground;

  public SnapButtonMaximized(Composite parent, int style) {
    super(parent, style);
    addPaintListener(new P_PaintListener());
    attachListeners();
    // colors
    m_borderColor = new Color(this.getDisplay(), 52, 1, 197);
    m_selectedBackground = new Color(this.getDisplay(), 200, 212, 249);
    setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
  }

  public void freeResources() {
    detachListeners();
    m_borderColor.dispose();
    m_selectedBackground.dispose();
  }

  protected void attachListeners() {
    addListener(SWT.FocusIn, m_controlListener);
    addListener(SWT.FocusOut, m_controlListener);
    addListener(SWT.MouseEnter, m_controlListener);
    addListener(SWT.MouseExit, m_controlListener);
    addListener(SWT.MouseUp, m_controlListener);
  }

  protected void detachListeners() {
    removeListener(SWT.FocusIn, m_controlListener);
    removeListener(SWT.FocusOut, m_controlListener);
    removeListener(SWT.MouseEnter, m_controlListener);
    removeListener(SWT.MouseExit, m_controlListener);
    removeListener(SWT.MouseUp, m_controlListener);
  }

  public void addSelectionListener(SelectionListener listener) {
    m_eventListener.add(SelectionListener.class, listener);
  }

  public void removeSelectionListener(SelectionListener listener) {
    m_eventListener.remove(SelectionListener.class, listener);
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);
    redraw();
  }

  public Image getImage() {
    return m_image;
  }

  public void setImage(Image image) {
    m_image = image;
    redraw();
  }

  public String getText() {
    return m_text;
  }

  public void setText(String text) {
    m_text = text;
    redraw();
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    int x = 0;
    int y = 0;
    if (getImage() != null) {
      Rectangle imgBounds = getImage().getBounds();
      x += imgBounds.width;
      y = Math.max(y, imgBounds.height);
    }
    if (getText() != null) {
      GC gc = null;
      try {
        gc = new GC(this);
        Point textSize = gc.stringExtent(getText());
        x += (textSize.x + m_gap);
        y = Math.max(textSize.y, y);
      }
      finally {
        if (gc != null) {
          gc.dispose();
        }
      }
    }
    x += (m_insets * 2 + m_borderInsets * 2 + m_borderWidth * 2);
    y += (m_insets * 2 + m_borderInsets * 2 + m_borderWidth * 2);
    return new Point(x, y);
  }

  private void handleSwtSelecion(Event e) {
    for (SelectionListener listener : m_eventListener.getListeners(SelectionListener.class)) {
      listener.widgetSelected(new SelectionEvent(e));
    }
  }

  private class P_ControlListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.FocusIn:
          m_focussed = true;
          break;
        case SWT.FocusOut:
          m_focussed = false;
          break;
        case SWT.MouseEnter:
          m_mouseHover = true;
          break;
        case SWT.MouseExit:
          m_mouseHover = false;
          break;
        case SWT.MouseUp:
          handleSwtSelecion(event);
          break;
      }
      redraw();
    }

  }

  private class P_PaintListener implements PaintListener {
    @Override
    public void paintControl(PaintEvent e) {
      Rectangle bounds = getClientArea();
      GC gc = e.gc;
      gc.setAdvanced(true);
      int inset = m_insets;
      int borderWidth = m_borderWidth;
      if (m_mouseHover) {
        inset--;
        borderWidth++;
      }
      gc.setBackground(m_borderColor);
      gc.fillRoundRectangle(inset, inset, bounds.width - inset, bounds.height - inset, 5, 5);
      gc.setBackground(getBackground());
      if (isSelected()) {
        gc.setBackground(m_selectedBackground);
      }
      gc.fillRoundRectangle(inset + borderWidth, inset + borderWidth, bounds.width - inset - 2 * borderWidth,
          bounds.height - inset - 2 * borderWidth, 5, 5);
      bounds.x = bounds.x + inset + borderWidth + m_borderInsets;
      bounds.y = bounds.y + inset + borderWidth + m_borderInsets;
      bounds.width = bounds.width - 2 * (inset + borderWidth + m_borderInsets);
      bounds.height = bounds.height - 2 * (inset + borderWidth + m_borderInsets);
      // image
      if (getImage() != null) {
        gc.drawImage(getImage(), bounds.x, bounds.y);
        bounds.x += (getImage().getBounds().width + m_gap);
      }
      if (getText() != null) {
        gc.drawText(getText(), bounds.x, bounds.y);
      }
    }
  } // end class P_PaintListener

  public boolean isSelected() {
    return m_selected;
  }

  public void setSelected(boolean selected) {
    m_selected = selected;
    redraw();
  }

}
