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

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

public class DropDownFileUpload extends FileUpload {
  private static final long serialVersionUID = 1L;

  private Rectangle m_buttonArea = new Rectangle(1, 1, 13, 17);
  private Rectangle m_dropDownArea = new Rectangle(14, 1, 10, 17);
  private EventListenerList m_eventListeners = new EventListenerList();

  private boolean m_dropdownEnabled = true;
  private boolean m_buttonEnabled = true;

  private String m_originalVariant = "";

  public DropDownFileUpload(Composite parent, int style) {
    super(parent, style | SWT.DOUBLE_BUFFERED);

    super.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSelectionInternal(e);
      }
    });

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
//FIXME selection event comes after a file has been selected, therefore a different solution has to be found to distinguish a click on the dropDownArea and the buttonArea (see also bugzilla: https://bugs.eclipse.org/bugs/show_bug.cgi?id=369423)
//    Point pt = new Point(event.x, event.y);
//    if (m_buttonArea.contains(pt)) {
    fireSelectionEvent(event);
//    }
//    else if (m_dropDownArea.contains(pt) && isDropdownEnabled()) {
//      Menu menu = createMenu();
//      if (menu != null) {
//        menu.setLocation(toDisplay(event.x, event.y));
//        menu.setVisible(true);
//      }
//    }
  }

  protected Menu createMenu() {
    Menu contextMenu = getMenu();
    if (getMenu() == null) {
      contextMenu = new Menu(getShell(), SWT.POP_UP);
      for (MenuListener listener : m_eventListeners.getListeners(MenuListener.class)) {
        contextMenu.addMenuListener(listener);
      }
      setMenu(contextMenu);
    }

    return contextMenu;
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

  public void addMenuListener(MenuListener listener) {
    m_eventListeners.add(MenuListener.class, listener);
  }

  public void removeMenuListener(MenuListener listener) {
    m_eventListeners.remove(MenuListener.class, listener);
  }

  public void setDropdownEnabled(boolean enabled) {
    m_dropdownEnabled = enabled;
    if (!StringUtility.hasText(m_originalVariant)) {
      m_originalVariant = (String) getData(RWT.CUSTOM_VARIANT);
    }
    String customVariant = m_dropdownEnabled ? m_originalVariant + "_menu" : m_originalVariant;
    setData(RWT.CUSTOM_VARIANT, customVariant);
    super.setEnabled(isButtonEnabled());
  }

  public boolean isDropdownEnabled() {
    return m_dropdownEnabled;
  }

  public void setButtonEnabled(boolean enabled) {
    m_buttonEnabled = enabled;
    super.setEnabled(isButtonEnabled());
  }

  public boolean isButtonEnabled() {
    return m_buttonEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    m_buttonEnabled = enabled;
    m_dropdownEnabled = enabled;
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

}
