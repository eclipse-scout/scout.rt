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
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class DropDownButton extends Canvas {

  private static final int BORDER = 3;
  private static final int GAP = 3;
  private String m_text = "";
  private Image m_image;
  private Image m_imageDisabled;
  private Rectangle m_buttonArea = new Rectangle(0, 0, 0, 0);
  private Rectangle m_dropDownArea = new Rectangle(0, 0, 0, 0);
  private EventListenerList m_eventListeners = new EventListenerList();

  private boolean m_mouseHover;
  private Point m_mouseDownPosition;
  private boolean m_dropdownEnabled = true;
  private boolean m_buttonEnabled = true;

  public DropDownButton(Composite parent, int style) {
    super(parent, style | SWT.DOUBLE_BUFFERED);
    addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent e) {
        paintInternal(e.gc);
      }
    });
    addListener(SWT.Traverse, new Listener() {
      @Override
      public void handleEvent(Event e) {
        switch (e.detail) {
          /* Do tab group traversal */
          case SWT.TRAVERSE_ESCAPE:
                  case SWT.TRAVERSE_RETURN:
                  case SWT.TRAVERSE_TAB_NEXT:
                  case SWT.TRAVERSE_TAB_PREVIOUS:
                  case SWT.TRAVERSE_PAGE_NEXT:
                  case SWT.TRAVERSE_PAGE_PREVIOUS:
                    e.doit = true;
                    break;
                }
              }
    });
    addListener(SWT.KeyDown, new Listener() {
      @Override
      public void handleEvent(Event e) {
        handleKeyEvent(e);
      }
    });

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        redraw();
      }

      @Override
      public void focusLost(FocusEvent e) {
        redraw();
      }
    });
    addMouseTrackListener(new MouseTrackAdapter() {
      @Override
      public void mouseEnter(MouseEvent e) {
        m_mouseHover = true;
        redraw();
      }

      @Override
      public void mouseExit(MouseEvent e) {
        m_mouseHover = false;
        redraw();
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        m_mouseDownPosition = new Point(e.x, e.y);
        redraw();
      }

      @Override
      public void mouseUp(MouseEvent e) {
        Point pt = new Point(e.x, e.y);
        if (e.button == 1 && getClientArea().contains(pt)) {
          handleSelectionInternal(e);
        }
        m_mouseDownPosition = null;
        redraw();
      }
    });

    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
    initResources();

  }

  protected void handleSelectionInternal(MouseEvent event) {
    Point pt = new Point(event.x, event.y);
    if (m_buttonArea.contains(pt)) {
      Event e = new Event();
      e.button = event.button;
      e.count = event.count;
      e.data = event.data;
      e.display = event.display;
      e.stateMask = event.stateMask;
      e.time = event.time;
      e.widget = event.widget;
      e.x = event.x;
      e.y = event.y;
      fireSelectionEvent(new SelectionEvent(e));
    }
    else if (m_dropDownArea.contains(pt) && isDropdownEnabled()) {
      if (getMenu() != null) {
        getMenu().setVisible(true);
      }
    }
  }

  protected void handleKeyEvent(Event e) {
    switch (e.keyCode) {
      case ' ':
      case SWT.CR:
        SelectionEvent selEvent = new SelectionEvent(e);
        fireSelectionEvent(selEvent);
        break;
      case SWT.ARROW_DOWN:
        if (isDropdownEnabled() && getMenu() != null) {
          getMenu().setVisible(true);
        }
        break;
    }
  }

  public void fireSelectionEvent(SelectionEvent e) {
    if (isButtonEnabled()) {
      for (SelectionListener l : m_eventListeners.getListeners(SelectionListener.class)) {
        l.widgetSelected(e);
      }
    }
  }

  public void addSelectionListener(SelectionListener listener) {
    m_eventListeners.add(SelectionListener.class, listener);
  }

  public void removeSelectionListener(SelectionListener listener) {
    m_eventListeners.remove(SelectionListener.class, listener);
  }

  public void setText(String text) {
    // ensure not null
    if (text == null) {
      text = "";
    }
    m_text = text;
    redraw();
  }

  public String getText() {
    return m_text;
  }

  public void setImage(Image image) {
    m_image = image;
    if (m_imageDisabled != null && !m_imageDisabled.isDisposed()) {
      m_imageDisabled.dispose();
      m_imageDisabled = null;
    }
    if (m_image != null) {
      m_imageDisabled = new Image(getDisplay(), m_image, SWT.IMAGE_DISABLE);
    }
    redraw();
  }

  public Image getImage() {
    return m_image;
  }

  public void setDropdownEnabled(boolean enabled) {
    m_dropdownEnabled = enabled;
    super.setEnabled(isButtonEnabled() || isDropdownEnabled());
    redraw();
  }

  public boolean isDropdownEnabled() {
    return m_dropdownEnabled;
  }

  public void setButtonEnabled(boolean enabled) {
    m_buttonEnabled = enabled;
    super.setEnabled(isButtonEnabled() || isDropdownEnabled());
    redraw();
  }

  public boolean isButtonEnabled() {
    return m_buttonEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    m_buttonEnabled = enabled;
    m_dropdownEnabled = enabled;
    redraw();

  }

  private Image m_dropDownIcon;
  private Image m_dropDownIconDisabled;
  private Color m_focusedHighlightBorderColor;
  private Color m_mouseOverHighlightColor;
  private Color m_borderColor;
  private Color m_borderColorDisabled;
  private Color m_textColor;
  private Color m_textColorDisabled;
  private Color m_backgroundGradient1;
  private Color m_backgroundGradient2;
  private Color m_backgroundGradient1MouseDown;
  private Color m_backgroundGradient2MouseDown;
  private Color m_backgroundDisabled;

  private void initResources() {
    m_borderColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    m_borderColorDisabled = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    m_textColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
    m_textColorDisabled = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    m_focusedHighlightBorderColor = new Color(getDisplay(), 118, 183, 232);
    m_mouseOverHighlightColor = new Color(getDisplay(), 180, 200, 220);
    m_dropDownIcon = Activator.imageDescriptorFromLocalPlugin(Activator.ICON_ARROW_DOWN).createImage();
    m_dropDownIconDisabled = new Image(getDisplay(), m_dropDownIcon, SWT.IMAGE_DISABLE);
    m_backgroundGradient1 = new Color(getDisplay(), 255, 255, 255);
    m_backgroundGradient2 = new Color(getDisplay(), 220, 220, 220);
    m_backgroundGradient1MouseDown = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    m_backgroundGradient2MouseDown = getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
    m_backgroundDisabled = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

  }

  public void freeResources() {
    m_dropDownIcon.dispose();
    m_dropDownIconDisabled.dispose();
    m_focusedHighlightBorderColor.dispose();
    m_mouseOverHighlightColor.dispose();
    m_backgroundGradient1.dispose();
    m_backgroundGradient2.dispose();
    if (m_imageDisabled != null && !m_imageDisabled.isDisposed()) {
      m_imageDisabled.dispose();
      m_imageDisabled = null;
    }
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    getClientArea();
    Point size = new Point(2 * BORDER + 2 * GAP, 2 * BORDER + 2 * GAP);
    GC gc = null;
    try {
      gc = new GC(this);
      if (getImage() != null) {
        Rectangle imgBounds = getImage().getBounds();
        size.x += (imgBounds.width + GAP);
        size.y = Math.max(size.y, (2 * (BORDER + GAP) + imgBounds.height));
      }
      if (!"".equals(getText())) {
        Point textSize = gc.textExtent(getText());
        size.x += textSize.x + GAP;
        size.y = Math.max(size.y, (2 * (BORDER + GAP) + textSize.y));
      }
      if ((getStyle() & SWT.DROP_DOWN) != 0) {
        size.x += (m_dropDownIcon.getBounds().width) /* splitline */+ 1;
        size.y = Math.max(size.y, 2 * (BORDER + GAP) + m_dropDownIcon.getBounds().height);
      }
    }
    finally {
      if (gc != null) {
        gc.dispose();
      }
    }
    size.x = Math.max(hint, size.x);
    size.y = Math.max(hint2, size.y);
    return size;
  }

  private void paintInternal(GC gc) {
    gc.setAdvanced(true);
    Color borderColor = (isEnabled()) ? (m_borderColor) : (m_borderColorDisabled);
    Rectangle clientArea = getClientArea();
    // clientArea.x +=1;
    // clientArea.y += 1;
    clientArea.width -= 1;
    clientArea.height -= 1;
    if (isFocusControl()) {
      gc.setBackground(m_focusedHighlightBorderColor);
      gc.fillRectangle(clientArea);
    }
    if (isFocusControl()) {
      gc.setForeground(borderColor);
      LineAttributes attr = new LineAttributes(1);
      attr.dashOffset = 1;
      attr.width = 1;
      attr.style = SWT.LINE_DOT;
      gc.setLineAttributes(attr);
      gc.drawRoundRectangle(clientArea.x + 2, clientArea.y + 2, clientArea.width - 4, clientArea.height - 4, 3, 3);
    }
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(borderColor);
    gc.drawRoundRectangle(clientArea.x, clientArea.y, clientArea.width, clientArea.height, 3, 3);

    clientArea.x += 1;
    clientArea.y += 1;
    clientArea.width -= 1;
    clientArea.height -= 1;
    // gc.setBackground(m_focusedHighlightBorderColor);
    // gc.fillRoundRectangle(clientArea.x, clientArea.y, clientArea.width,
    // clientArea.height, 3, 3);

    // split in dropdown and buttonArea

    Rectangle buttonArea = createCopy(clientArea);
    m_dropDownArea = new Rectangle(0, 0, 0, 0);
    if ((getStyle() & SWT.DROP_DOWN) != 0) {
      int witdh = m_dropDownIcon.getBounds().width + 1;
      int x = buttonArea.x + buttonArea.width - witdh;
      int y = buttonArea.y;
      int height = buttonArea.height;
      Rectangle dropdownArea = new Rectangle(x, y, witdh, height);
      m_dropDownArea = createCopy(dropdownArea);
      buttonArea.width -= dropdownArea.width;
      paintDropdownButton(gc, dropdownArea);
    }
    m_buttonArea = createCopy(buttonArea);
    paintButton(gc, buttonArea);
  }

  private Rectangle createCopy(Rectangle rect) {
    Rectangle copy = new Rectangle(rect.x, rect.y, rect.width, rect.height);
    return copy;
  }

  private void paintButton(GC gc, Rectangle drawArea) {

    if (!isButtonEnabled()) {
      gc.setForeground(m_backgroundDisabled);
      gc.setBackground(m_backgroundDisabled);
    }
    else if (m_mouseDownPosition != null && drawArea.contains(m_mouseDownPosition)) {
      gc.setForeground(m_backgroundGradient1MouseDown);
      gc.setBackground(m_backgroundGradient2MouseDown);
    }
    else if (m_mouseHover) {
      gc.setForeground(m_backgroundGradient1);
      gc.setBackground(m_mouseOverHighlightColor);
    }
    else {
      gc.setForeground(m_backgroundGradient1);
      gc.setBackground(m_backgroundGradient2);
    }
    gc.fillGradientRectangle(drawArea.x, drawArea.y, drawArea.width, drawArea.height, true);
    drawArea.x += GAP;
    if (m_image != null) {
      Image img = (isButtonEnabled()) ? (m_image) : (m_imageDisabled);
      gc.drawImage(img, drawArea.x, Math.max(drawArea.y + GAP, drawArea.y + (drawArea.height - img.getBounds().height) / 2));
      drawArea.x += img.getBounds().width + GAP;
      drawArea.width -= img.getBounds().width + GAP;
    }
    if (!"".equals(getText())) {
      Point textSize = gc.textExtent(getText());
      int x = drawArea.x + Math.max(0, (drawArea.width - textSize.x) / 2);
      int y = drawArea.y + Math.max(GAP, (drawArea.height - textSize.y) / 2);
      if (isButtonEnabled()) {
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
      }
      else {
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
      }
      gc.drawText(getText(), x, y, true);
    }
  }

  private void paintDropdownButton(GC gc, Rectangle bounds) {
    if (!isDropdownEnabled()) {
      gc.setForeground(m_backgroundDisabled);
      gc.setBackground(m_backgroundDisabled);
    }
    else if (m_mouseDownPosition != null && bounds.contains(m_mouseDownPosition)) {
      gc.setForeground(m_backgroundGradient1MouseDown);
      gc.setBackground(m_backgroundGradient2MouseDown);
    }
    else {
      gc.setForeground(m_backgroundGradient1);
      gc.setBackground(m_backgroundGradient2);
    }
    gc.fillGradientRectangle(bounds.x, bounds.y, bounds.width, bounds.height, true);
    // dropdown icon
    Image img = null;
    if (isDropdownEnabled()) {
      img = m_dropDownIcon;
      gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
    }
    else {
      img = m_dropDownIconDisabled;
      gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
    }
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
    bounds.x += 1;
    int imgY = Math.max(bounds.y + GAP, bounds.y + (bounds.height - img.getBounds().height) / 2);
    gc.drawImage(img, bounds.x, imgY);
  }

}
