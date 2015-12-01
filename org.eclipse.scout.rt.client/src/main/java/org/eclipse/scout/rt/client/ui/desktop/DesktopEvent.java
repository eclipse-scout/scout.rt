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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class DesktopEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_DESKTOP_CLOSED = 100;
  /**
   * Event type that indicates that the active outline changes.
   *
   * @see IDesktop#setOutline(IOutline)
   */
  public static final int TYPE_OUTLINE_CHANGED = 200;

  /**
   * Event type that indicates that the outline content needs to be activated.
   *
   * @see IDesktop#activateOutline(IOutline)
   */
  public static final int TYPE_OUTLINE_CONTENT_ACTIVATE = 210;

  /**
   * @deprecated use {@link #TYPE_FORM_SHOW}; will be removed in version 6.1.
   */
  @Deprecated
  public static final int TYPE_FORM_ADDED = 600;
  public static final int TYPE_FORM_SHOW = 600;
  /**
   * @deprecated use {@link #TYPE_FORM_HIDE}; will be removed in version 6.1.
   */
  @Deprecated
  public static final int TYPE_FORM_REMOVED = 610;
  public static final int TYPE_FORM_HIDE = 610;

  /**
   * @deprecated use {@link #TYPE_FORM_ACTIVATE}; will be removed in version 6.1.
   */
  @Deprecated
  public static final int TYPE_FORM_ENSURE_VISIBLE = 620;
  public static final int TYPE_FORM_ACTIVATE = 620;

  /**
   * @deprecated use {@link #TYPE_MESSAGE_BOX_SHOW}; will be removed in version 6.1.
   */
  @Deprecated
  public static final int TYPE_MESSAGE_BOX_ADDED = 700;
  public static final int TYPE_MESSAGE_BOX_SHOW = 700;
  /**
   * @deprecated use {@link #TYPE_MESSAGE_BOX_HIDE}; will be removed in version 6.1.
   */
  @Deprecated
  public static final int TYPE_MESSAGE_BOX_REMOVED = 710;
  public static final int TYPE_MESSAGE_BOX_HIDE = 710;
  /**
   * print a form using properties printDevice, printParameters
   */
  public static final int TYPE_PRINT = 900;

  public static final int TYPE_PRINTED = 901;

  /**
   * @deprecated use {@link #TYPE_FILE_CHOOSER_SHOW}; will be removed in version 6.1.
   */
  @Deprecated
  public static final int TYPE_FILE_CHOOSER_ADDED = 910;
  public static final int TYPE_FILE_CHOOSER_SHOW = 910;

  public static final int TYPE_FILE_CHOOSER_HIDE = 915;

  /**
   * Opens a given URI using {@link #getUri()} or {@link #getBinaryResource()}.
   *
   * @see IDesktop#openUri(String, IOpenUriAction)
   */
  public static final int TYPE_OPEN_URI = 920;
  /**
   * Send a broadcast event to find the {@link IFormField} that owns the focus The listener can store the result using
   * {@link #setFocusedField()} The event waits some time to give asynchronous jobs a chance to complete (default is
   * 2000 ms)
   */
  public static final int TYPE_FIND_FOCUS_OWNER = 1000;
  /**
   * Broadcast request to add all popup menus for the tray menu collector: popupMenus
   */
  public static final int TYPE_TRAY_POPUP = 1010;

  /**
   * Event type that indicates that the desktop should traverse the focus to the next possible location.
   *
   * @see IDesktop#traverseFocusNext()
   */
  public static final int TYPE_TRAVERSE_FOCUS_NEXT = 1020;

  /**
   * Event type that indicates that the desktop should traverse the focus to the previous location.
   *
   * @see IDesktop#traverseFocusPrevious()
   */
  public static final int TYPE_TRAVERSE_FOCUS_PREVIOUS = 1030;

  private final int m_type;
  private IOutline m_outline;
  private IForm m_form;
  private IForm m_activeForm;
  private IFormField m_focusedField;
  private IMessageBox m_messageBox;
  private IFileChooser m_fileChooser;
  private String m_uri;
  private PrintDevice m_printDevice;
  private Map<String, Object> m_printParameters;
  private List<IMenu> m_popupMenus;
  private File m_printedFile;
  private IOpenUriAction m_openUriAction;
  private BinaryResource m_binaryResource;

  public DesktopEvent(IDesktop source, int type) {
    super(source);
    m_type = type;
  }

  public DesktopEvent(IDesktop source, int type, File printedFile) {
    this(source, type);
    m_printedFile = printedFile;
  }

  public DesktopEvent(IDesktop source, int type, IForm form) {
    this(source, type);
    m_form = form;
  }

  public DesktopEvent(IDesktop source, int type, IMessageBox messageBox) {
    this(source, type);
    m_messageBox = messageBox;
  }

  public DesktopEvent(IDesktop source, int type, IOutline outline) {
    this(source, type);
    m_outline = outline;
  }

  public DesktopEvent(IDesktop source, int type, IFileChooser fc) {
    this(source, type);
    m_fileChooser = fc;
  }

  public DesktopEvent(IDesktop source, int type, String uri, IOpenUriAction openUriAction) {
    this(source, type);
    m_uri = uri;
    m_openUriAction = openUriAction;
  }

  public DesktopEvent(IDesktop source, int type, BinaryResource res, IOpenUriAction openUriAction) {
    this(source, type);
    m_binaryResource = res;
    m_openUriAction = openUriAction;
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

  @Override
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

  public String getUri() {
    return m_uri;
  }

  public IOpenUriAction getOpenUriAction() {
    return m_openUriAction;
  }

  public BinaryResource getBinaryResource() {
    return m_binaryResource;
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

  public File getPrintedFile() {
    return m_printedFile;
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public void addPopupMenu(IMenu menu) {
    if (menu != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.add(menu);
    }
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public void addPopupMenus(List<IMenu> menus) {
    if (menus != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.addAll(menus);
    }
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public List<IMenu> getPopupMenus() {
    return CollectionUtility.arrayList(m_popupMenus);
  }

  /**
   * used by TYPE_TRAY_POPUP to add menus
   */
  public int getPopupMenuCount() {
    if (m_popupMenus != null) {
      return m_popupMenus.size();
    }
    else {
      return 0;
    }
  }

  public Map<String, Object> getPrintParameters() {
    return CollectionUtility.copyMap(m_printParameters);
  }

  public IForm getActiveForm() {
    return m_activeForm;
  }

  public void setActiveForm(IForm activeForm) {
    m_activeForm = activeForm;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
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
    catch (Exception t) {
      buf.append("#" + m_type);
    }
    if (m_form != null) {
      buf.append(" " + m_form.getTitle());
    }
    if (m_messageBox != null) {
      buf.append(" " + m_messageBox.getHeader());
    }
    if (m_outline != null) {
      buf.append(" " + m_outline.getRootNode().getCell().getText());
    }
    buf.append("]");
    return buf.toString();
  }
}
