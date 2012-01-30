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

import java.util.Date;

import org.eclipse.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.AbstractOpenMenuJob;
import org.eclipse.scout.rt.ui.rap.core.RwtIcons;
import org.eclipse.scout.rt.ui.rap.core.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

@SuppressWarnings("restriction")
public class DropDownButton extends Button implements IDropDownButtonForPatch {
  private static final long serialVersionUID = 1L;

  private static final int BORDER = 3;
  private static final int GAP = 3;
  private String m_text = "";
  private Image m_image;
  private Image m_imageDisabled;
  private Rectangle m_buttonArea = new Rectangle(1, 1, 13, 17);
  private Rectangle m_dropDownArea = new Rectangle(14, 1, 10, 17);
  private EventListenerList m_eventListeners = new EventListenerList();

  private boolean m_mouseHover;
  private Point m_mouseDownPosition;
  private boolean m_dropdownEnabled = true;
  private boolean m_buttonEnabled = true;

  private String m_originalVariant = "";

  public DropDownButton(Composite parent, int style) {
    super(parent, style | SWT.DOUBLE_BUFFERED);
    addListener(SWT.Traverse, new Listener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void handleEvent(Event e) {
        switch (e.detail) {
          /* Do tab group traversal */
          case SWT.TRAVERSE_ESCAPE:
          case SWT.TRAVERSE_RETURN:
          case SWT.TRAVERSE_TAB_NEXT:
          case SWT.TRAVERSE_TAB_PREVIOUS:
            e.doit = true;
            break;
        }
      }
    });
    getUiEnvironment().addKeyStroke(this, new P_KeyStroke(' '), false);
    getUiEnvironment().addKeyStroke(this, new P_KeyStroke(SWT.CR), false);
    getUiEnvironment().addKeyStroke(this, new P_KeyStroke(SWT.ARROW_DOWN), false);

    addFocusListener(new FocusAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void focusGained(FocusEvent e) {
        redraw();
      }

      @Override
      public void focusLost(FocusEvent e) {
        redraw();
      }
    });
    addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      private long m_mouseDownTime = 0;
      private P_OpenMenuJob m_openMenuJob = new P_OpenMenuJob(DropDownButton.this);

      @Override
      public void mouseDown(MouseEvent event) {
        m_mouseDownPosition = new Point(event.x, event.y);
        if (isDropdownEnabled()) {
          m_mouseDownTime = new Date().getTime();
          m_openMenuJob.startOpenJob(m_mouseDownPosition);
        }
        redraw();
      }

      @Override
      public void mouseUp(MouseEvent event) {
        if (event.button == 1) {
          BrowserInfo browserInfo = RwtUtility.getBrowserInfo();
          if (browserInfo.isTablet()
              || browserInfo.isMobile()) {
            long mouseUpTime = new Date().getTime();
            if (mouseUpTime - m_mouseDownTime <= 500L) {
              m_openMenuJob.stopOpenJob();
            }
          }
          handleSelectionInternal(event);
        }
        m_mouseDownPosition = null;
        redraw();
      }
    });

    addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });

//    addPaintListener(new PaintListener() {
//
//      @Override
//      public void paintControl(PaintEvent event) {
//        paintInternal(event.gc);
//      }
//    });
    initResources();
  }

  /**
   * since tab list on parent does not work
   */
  @Override
  public boolean forceFocus() {
    if ((getStyle() & SWT.NO_FOCUS) != 0) {
      return false;
    }
    else {
      return super.forceFocus();
    }
  }

  protected void handleSelectionInternal(MouseEvent event) {
    Point pt = new Point(event.x, event.y);
    if (m_buttonArea.contains(pt)) {
      Event e = new Event();
      e.button = event.button;
      e.count = 1;
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
        getMenu().setLocation(toDisplay(event.x, event.y));
        getMenu().setVisible(true);
      }
    }
  }

  @Override
  public void fireSelectionEvent(SelectionEvent e) {
    if (isButtonEnabled()) {
      for (SelectionListener l : m_eventListeners.getListeners(SelectionListener.class)) {
        l.widgetSelected(e);
      }
    }
  }

  @Override
  public void addSelectionListener(SelectionListener listener) {
    m_eventListeners.add(SelectionListener.class, listener);
  }

  @Override
  public void removeSelectionListener(SelectionListener listener) {
    m_eventListeners.remove(SelectionListener.class, listener);
  }

  @Override
  public void setText(String text) {
    // ensure not null
    if (text == null) {
      text = "";
    }
    m_text = text;
    redraw();
  }

  @Override
  public String getText() {
    return m_text;
  }

  @Override
  public void setImage(Image image) {
    m_image = image;
    if (m_imageDisabled != null && !m_imageDisabled.isDisposed() && m_imageDisabled.getDevice() != null) {
      m_imageDisabled.dispose();
      m_imageDisabled = null;
    }
    if (m_image != null) {
      m_imageDisabled = new Image(getDisplay(), m_image, SWT.IMAGE_COPY);
    }
    redraw();
  }

  @Override
  public Image getImage() {
    return m_image;
  }

  @Override
  public void setDropdownEnabled(boolean enabled) {
    m_dropdownEnabled = enabled;
    if (!StringUtility.hasText(m_originalVariant)) {
      m_originalVariant = (String) getData(WidgetUtil.CUSTOM_VARIANT);
    }
    String customVariant = m_dropdownEnabled ? m_originalVariant + "_menu" : m_originalVariant;
    setData(WidgetUtil.CUSTOM_VARIANT, customVariant);
//    super.setEnabled(isButtonEnabled() || isDropdownEnabled());XXX
    redraw();
  }

  @Override
  public boolean isDropdownEnabled() {
    return m_dropdownEnabled;
  }

  @Override
  public void setButtonEnabled(boolean enabled) {
    m_buttonEnabled = enabled;
//  super.setEnabled(isButtonEnabled() || isDropdownEnabled());XXX
    super.setEnabled(isButtonEnabled());
    redraw();
  }

  @Override
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

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
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

  private void initResources() {//FIXME sle colors have to be defined in css
    m_borderColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    m_borderColorDisabled = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    m_textColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
    m_textColorDisabled = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    m_focusedHighlightBorderColor = new Color(getDisplay(), 118, 183, 232);
    m_mouseOverHighlightColor = new Color(getDisplay(), 180, 200, 220);
    m_dropDownIcon = getUiEnvironment().getIcon(RwtIcons.DropDownFieldArrowDown);
    m_dropDownIconDisabled = new Image(getDisplay(), m_dropDownIcon, SWT.IMAGE_COPY);//XXX rap
    m_backgroundGradient1 = new Color(getDisplay(), 255, 255, 255);
    m_backgroundGradient2 = new Color(getDisplay(), 220, 220, 220);
    m_backgroundGradient1MouseDown = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    m_backgroundGradient2MouseDown = getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
    m_backgroundDisabled = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

  }

  @Override
  public void freeResources() {
    if (m_dropDownIcon != null && !m_dropDownIcon.isDisposed() && m_dropDownIcon.getDevice() != null) {
      m_dropDownIcon.dispose();
      m_dropDownIcon = null;
    }
    if (m_dropDownIconDisabled != null && !m_dropDownIconDisabled.isDisposed() && m_dropDownIconDisabled.getDevice() != null) {
      m_dropDownIconDisabled.dispose();
      m_dropDownIconDisabled = null;
    }
    if (m_focusedHighlightBorderColor != null && !m_focusedHighlightBorderColor.isDisposed() && m_focusedHighlightBorderColor.getDevice() != null) {
      m_focusedHighlightBorderColor.dispose();
      m_focusedHighlightBorderColor = null;
    }
    if (m_mouseOverHighlightColor != null && !m_mouseOverHighlightColor.isDisposed() && m_mouseOverHighlightColor.getDevice() != null) {
      m_mouseOverHighlightColor.dispose();
      m_mouseOverHighlightColor = null;
    }
    if (m_backgroundGradient1 != null && !m_backgroundGradient1.isDisposed() && m_backgroundGradient1.getDevice() != null) {
      m_backgroundGradient1.dispose();
      m_backgroundGradient1 = null;
    }
    if (m_backgroundGradient2 != null && !m_backgroundGradient2.isDisposed() && m_backgroundGradient2.getDevice() != null) {
      m_backgroundGradient2.dispose();
      m_backgroundGradient2 = null;
    }
    if (m_imageDisabled != null && !m_imageDisabled.isDisposed() && m_imageDisabled.getDevice() != null) {
      m_imageDisabled.dispose();
      m_imageDisabled = null;
    }
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
//    getClientArea();XXX RAP
    Point size = new Point(2 * BORDER + 2 * GAP, 2 * BORDER + 2 * GAP);
    if (getImage() != null) {
      Rectangle imgBounds = getImage().getBounds();
      size.x += (imgBounds.width + GAP);
      size.y = Math.max(size.y, (2 * (BORDER + GAP) + imgBounds.height));
    }
    if (!"".equals(getText())) {
      Point textSize = TextSizeUtil.stringExtent(getFont(), getText());
      size.x += textSize.x + GAP;
      size.y = Math.max(size.y, (2 * (BORDER + GAP) + textSize.y));
    }
    if (m_dropDownIcon != null && m_dropDownIcon.isDisposed()) {
      m_dropDownIcon = getUiEnvironment().getIcon(RwtIcons.DropDownFieldArrowDown);
    }
    if ((getStyle() & SWT.DROP_DOWN) != 0 && m_dropDownIcon != null && !m_dropDownIcon.isDisposed()) {
      size.x += (m_dropDownIcon.getBounds().width) /* splitline */+ 1;
      size.y = Math.max(size.y, 2 * (BORDER + GAP) + m_dropDownIcon.getBounds().height);
    }
    size.x = Math.max(hint, size.x);
    size.y = Math.max(hint2, size.y);
    return size;
  }

  @Override
  public void setBounds(Rectangle bounds) {
    super.setBounds(bounds);
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
  }

//  private void paintInternal(GC gc) {XXX RAP
//    gc.setAdvanced(true);
////    Color borderColor = (isEnabled()) ? (m_borderColor) : (m_borderColorDisabled);
//    Rectangle clientArea = getClientArea();
//    clientArea.width -= 1;
//    clientArea.height -= 1;
////    gc.setLineStyle(SWT.LINE_SOLID);XXX RAP
////    gc.setForeground(borderColor);
////    gc.drawRoundRectangle(clientArea.x, clientArea.y, clientArea.width, clientArea.height, 3, 3);
//
//    clientArea.x += 1;
//    clientArea.y += 1;
//    clientArea.width -= 1;
//    clientArea.height -= 1;
//
//    // split in dropdown and buttonArea
//
//    Rectangle buttonArea = createCopy(clientArea);
//    m_dropDownArea = new Rectangle(0, 0, 0, 0);
//    if ((getStyle() & SWT.DROP_DOWN) != 0) {
//      int witdh = m_dropDownIcon.getBounds().width + 1;
//      int x = buttonArea.x + buttonArea.width - witdh;
//      int y = buttonArea.y;
//      int height = buttonArea.height;
//      Rectangle dropdownArea = new Rectangle(x, y, witdh, height);
//      m_dropDownArea = createCopy(dropdownArea);
//      buttonArea.width -= dropdownArea.width;
//      paintDropdownButton(gc, dropdownArea);
//    }
//    m_buttonArea = createCopy(buttonArea);
//    paintButton(gc, buttonArea);
//  }

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
    //XXX rap    gc.setLineStyle(SWT.LINE_SOLID);
    gc.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
    bounds.x += 1;
    int imgY = Math.max(bounds.y + GAP, bounds.y + (bounds.height - img.getBounds().height) / 2);
    gc.drawImage(img, bounds.x, imgY);
  }

  private class P_KeyStroke extends RwtKeyStroke {
    public P_KeyStroke(int keyCode) {
      super(keyCode);
    }

    @Override
    public void handleUiAction(Event e) {
      switch (e.keyCode) {
        case ' ':
        case SWT.CR:
          SelectionEvent selEvent = new SelectionEvent(e);
          fireSelectionEvent(selEvent);
          break;
        case SWT.ARROW_DOWN:
          if (isDropdownEnabled() && getMenu() != null) {
            getMenu().setLocation(toDisplay(e.x, e.y));
            getMenu().setVisible(true);
          }
          break;
      }
    }
  }

  private final class P_OpenMenuJob extends AbstractOpenMenuJob {

    public P_OpenMenuJob(Control UiField) {
      super(UiField);
    }

    @Override
    public void showMenu(Point pt) {
      getMenu().setLocation(pt);
      getMenu().setVisible(true);
    }

    @Override
    public boolean openMenuCheck() {
      return isDropdownEnabled()
          && !isDisposed()
          && getMenu() != null
          && !getMenu().isDisposed();
    }
  }
}
