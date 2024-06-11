/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

@SuppressWarnings({"serial", "squid:S2057"})
public class DesktopEvent extends EventObject implements IModelEvent {

  public static final int TYPE_DESKTOP_CLOSED = 100;
  /**
   * Event type that indicates that the active outline changes.
   *
   * @see IDesktop#setOutline(Class)
   */
  public static final int TYPE_OUTLINE_CHANGED = 200;

  /**
   * Event type that indicates that the outline content needs to be activated.
   *
   * @see IDesktop#activateOutline(IOutline)
   */
  public static final int TYPE_OUTLINE_CONTENT_ACTIVATE = 210;

  public static final int TYPE_FORM_SHOW = 600;
  public static final int TYPE_FORM_HIDE = 610;
  public static final int TYPE_FORM_ACTIVATE = 620;
  public static final int TYPE_MESSAGE_BOX_SHOW = 700;
  public static final int TYPE_MESSAGE_BOX_HIDE = 710;
  public static final int TYPE_FILE_CHOOSER_SHOW = 910;

  public static final int TYPE_FILE_CHOOSER_HIDE = 915;

  /**
   * Opens a given URI using {@link #getUri()} or {@link #getBinaryResource()}.
   *
   * @see IDesktop#openUri(String, IOpenUriAction)
   */
  public static final int TYPE_OPEN_URI = 920;

  /**
   * Event type indicates that a notification has been added to the desktop.
   */
  public static final int TYPE_NOTIFICATION_ADDED = 1040;

  /**
   * Event type indicates that a notification has been removed from the desktop.
   */
  public static final int TYPE_NOTIFICATION_REMOVED = 1050;

  /**
   * Event type to trigger a GUI reload.
   */
  public static final int TYPE_RELOAD_GUI = 1060;

  private final int m_type;
  private IOutline m_outline;
  private IForm m_form;
  private IForm m_activeForm;
  private IMessageBox m_messageBox;
  private IFileChooser m_fileChooser;
  private String m_uri;
  private IOpenUriAction m_openUriAction;
  private BinaryResource m_binaryResource;
  private IDesktopNotification m_notification;

  public DesktopEvent(IDesktop source, int type) {
    super(source);
    m_type = type;
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

  public DesktopEvent(IDesktop source, int type, IDesktopNotification notification) {
    super(source);
    m_type = type;
    m_notification = notification;
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

  public IForm getActiveForm() {
    return m_activeForm;
  }

  public void setActiveForm(IForm activeForm) {
    m_activeForm = activeForm;
  }

  public IDesktopNotification getNotification() {
    return m_notification;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getClass().getSimpleName()).append("[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (Field aF : f) {
        if (Modifier.isPublic(aF.getModifiers())
            && Modifier.isStatic(aF.getModifiers())
            && aF.getName().startsWith("TYPE_")
            && ((Number) aF.get(null)).intValue() == m_type) {
          buf.append(aF.getName());
          break;
        }
      }
    }
    catch (Exception t) { // NOSONAR
      buf.append("#").append(m_type);
    }
    if (m_form != null) {
      buf.append(" ").append(m_form.getTitle());
    }
    if (m_messageBox != null) {
      buf.append(" ").append(m_messageBox.getHeader());
    }
    if (m_outline != null) {
      buf.append(" ").append(m_outline.getRootNode().getCell().toPlainText());
    }
    buf.append("]");
    return buf.toString();
  }
}
