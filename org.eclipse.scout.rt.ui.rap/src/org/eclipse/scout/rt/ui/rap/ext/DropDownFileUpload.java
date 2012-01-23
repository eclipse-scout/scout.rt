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

import org.eclipse.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.rwt.widgets.FileUpload;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.core.RwtIcons;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

@SuppressWarnings("restriction")
public class DropDownFileUpload extends FileUpload {
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
  private boolean m_dropdownEnabled = true;
  private boolean m_buttonEnabled = true;

  private String m_originalVariant = "";

  public DropDownFileUpload(Composite parent, int style) {
    super(parent, style | SWT.DOUBLE_BUFFERED);
    setBackgroundPosition(SWT.BACKGROUND_POSITION_LEFT_CENTER);
    setBackgroundRepeat(SWT.BACKGROUND_REPEAT_NO_REPEAT);

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
    super.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSelectionInternal(e);
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

  protected void handleSelectionInternal(SelectionEvent event) {
//FIXME there is a open bugzilla: https://bugs.eclipse.org/bugs/show_bug.cgi?id=369423
//    Point pt = new Point(event.x, event.y);
//    if (m_buttonArea.contains(pt)) {
    fireSelectionEvent(event);
//    }
//    else if (m_dropDownArea.contains(pt) && isDropdownEnabled()) {
//      if (getMenu() != null) {
//        getMenu().setLocation(toDisplay(event.x, event.y));
//        getMenu().setVisible(true);
//      }
//    }
  }

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

  public boolean isDropdownEnabled() {
    return m_dropdownEnabled;
  }

  public void setButtonEnabled(boolean enabled) {
    m_buttonEnabled = enabled;
//  super.setEnabled(isButtonEnabled() || isDropdownEnabled());XXX
    super.setEnabled(isButtonEnabled());
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

  private Rectangle createCopy(Rectangle rect) {
    Rectangle copy = new Rectangle(rect.x, rect.y, rect.width, rect.height);
    return copy;
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
}
