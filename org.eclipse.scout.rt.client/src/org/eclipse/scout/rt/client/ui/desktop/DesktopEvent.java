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
package org.eclipse.scout.rt.client.ui.desktop;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;

public class DesktopEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_DESKTOP_CLOSED = 100;
  /**
   * A new page is activated
   */
  public static final int TYPE_OUTLINE_CHANGED = 200;
  public static final int TYPE_FORM_ADDED = 600;
  /**
   * Necessary for page forms that "close" in the gui even if they are not
   * closed in model
   */
  public static final int TYPE_FORM_REMOVED = 610;
  public static final int TYPE_FORM_ENSURE_VISIBLE = 620;
  public static final int TYPE_MESSAGE_BOX_ADDED = 700;
  /**
   * print a form using properties printDevice, printParameters
   */
  public static final int TYPE_PRINT = 900;
  public static final int TYPE_FILE_CHOOSER_ADDED = 910;
  /**
   * Send a broadcast event to find the {@link IFormField} that owns the focus
   * The listener can store the result using {@link #setFocusedField()} The event waits some time to give asynchronous
   * jobs a chance to complete (default is 2000 ms)
   */
  public static final int TYPE_FIND_FOCUS_OWNER = 1000;
  /**
   * Broadcast request to add all popup menus for the tray menu
   * collector: popupMenus
   */
  public static final int TYPE_TRAY_POPUP = 1010;

  private final int m_type;
  private IOutline m_outline;
  private IForm m_form;
  private IFormField m_focusedField;
  private IMessageBox m_messageBox;
  private IFileChooser m_fileChooser;
  private PrintDevice m_printDevice;
  private Map<String, Object> m_printParameters;
  private List<IMenu> m_popupMenus;

  public DesktopEvent(IDesktop source, int type) {
    super(source);
    m_type = type;
  }

  public DesktopEvent(IDesktop source, int type, IForm form) {
    super(source);
    m_type = type;
    m_form = form;
  }

  public DesktopEvent(IDesktop source, int type, IMessageBox messageBox) {
    super(source);
    m_type = type;
    m_messageBox = messageBox;
  }

  public DesktopEvent(IDesktop source, int type, IOutline outline) {
    super(source);
    m_type = type;
    m_outline = outline;
  }

  public DesktopEvent(IDesktop source, int type, IFileChooser fc) {
    super(source);
    m_type = type;
    m_fileChooser = fc;
  }

  public DesktopEvent(IDesktop source, int type, PrintDevice printDevice, Map<String, Object> printParameters) {
    super(source);
    m_type = type;
    m_printDevice = printDevice;
    m_printParameters = printParameters;
  }

  public IDesktop getDesktop() {
    return (IDesktop) getSource();
  }

  public int getType() {
    return m_type;
  }

  public IForm getForm() {
    return m_form;
  }

  public ISearchForm getSearchForm() {
    return (ISearchForm) m_form;
  }

  public IForm getDetailForm() {
    return m_form;
  }

  public IFileChooser getFileChooser() {
    return m_fileChooser;
  }

  public IMessageBox getMessageBox() {
    return m_messageBox;
  }

  public IOutline getOutline() {
    return m_outline;
  }

  public IFormField getFocusedField() {
    return m_focusedField;
  }

  public void setFocusedField(IFormField f) {
    m_focusedField = f;
  }

  public PrintDevice getPrintDevice() {
    return m_printDevice;
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public void addPopupMenu(IMenu menu) {
    if (menu != null) {
      if (m_popupMenus == null) m_popupMenus = new ArrayList<IMenu>();
      m_popupMenus.add(menu);
    }
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public void addPopupMenus(IMenu[] menus) {
    if (menus != null) {
      if (m_popupMenus == null) m_popupMenus = new ArrayList<IMenu>();
      m_popupMenus.addAll(Arrays.asList(menus));
    }
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public IMenu[] getPopupMenus() {
    if (m_popupMenus != null) return m_popupMenus.toArray(new IMenu[0]);
    else return new IMenu[0];
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public int getPopupMenuCount() {
    if (m_popupMenus != null) return m_popupMenus.size();
    else return 0;
  }

  public Map<String, Object> getPrintParameters() {
    return new HashMap<String, Object>(m_printParameters);
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getClass().getSimpleName() + "[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (int i = 0; i < f.length; i++) {
        if (Modifier.isPublic(f[i].getModifiers()) && Modifier.isStatic(f[i].getModifiers()) && f[i].getName().startsWith("TYPE_")) {
          if (((Number) f[i].get(null)).intValue() == m_type) {
            buf.append(f[i].getName());
            break;
          }
        }
      }
    }
    catch (Throwable t) {
      buf.append("#" + m_type);
    }
    if (m_form != null) {
      buf.append(" " + m_form.getTitle());
    }
    if (m_messageBox != null) {
      buf.append(" " + m_messageBox.getTitle());
    }
    if (m_outline != null) {
      buf.append(" " + m_outline.getRootNode().getCell().getText());
    }
    buf.append("]");
    return buf.toString();
  }
}
