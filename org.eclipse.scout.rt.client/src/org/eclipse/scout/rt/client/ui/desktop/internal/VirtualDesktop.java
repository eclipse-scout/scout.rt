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
package org.eclipse.scout.rt.client.ui.desktop.internal;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopUIFacade;
import org.eclipse.scout.rt.client.ui.desktop.IUrlTarget;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 *  Copyright (c) 2001,2008 BSI AG
 * @version 3.x
 */

/**
 * This class is used as a placeholder of virtual desktop while the desktop is
 * loading until the desktop is set onto the {@link IClientSession}. Reasons for
 * that are observer attachments and data change registration in the init block
 * of forms, fields, pages that must be done while the desktop is loading. This
 * pattern solves the bird/egg problem in initialization of an object with
 * self-references.
 */
public class VirtualDesktop implements IDesktop {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(VirtualDesktop.class);

  private final EventListenerList m_listenerList;
  private final Map<String, EventListenerList> m_propertyChangeListenerMap;
  private final Map<Object, EventListenerList> m_dataChangeListenerMap;

  public VirtualDesktop() {
    m_listenerList = new EventListenerList();
    m_propertyChangeListenerMap = new HashMap<String, EventListenerList>();
    m_dataChangeListenerMap = new HashMap<Object, EventListenerList>();
  }

  public DesktopListener[] getDesktopListeners() {
    return m_listenerList.getListeners(DesktopListener.class);
  }

  public Map<Object, EventListenerList> getDataChangeListenerMap() {
    return m_dataChangeListenerMap;
  }

  public Map<String, EventListenerList> getPropertyChangeListenerMap() {
    return m_propertyChangeListenerMap;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    EventListenerList list = m_propertyChangeListenerMap.get(null);
    if (list == null) {
      list = new EventListenerList();
      m_propertyChangeListenerMap.put(null, list);
    }
    list.add(PropertyChangeListener.class, listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    EventListenerList list = m_propertyChangeListenerMap.get(propertyName);
    if (list == null) {
      list = new EventListenerList();
      m_propertyChangeListenerMap.put(propertyName, list);
    }
    list.add(PropertyChangeListener.class, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    for (Iterator<EventListenerList> it = m_dataChangeListenerMap.values().iterator(); it.hasNext();) {
      EventListenerList list = it.next();
      list.remove(PropertyChangeListener.class, listener);
      if (list.getListenerCount(DataChangeListener.class) == 0) {
        it.remove();
      }
    }
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    for (Iterator<EventListenerList> it = m_dataChangeListenerMap.values().iterator(); it.hasNext();) {
      EventListenerList list = it.next();
      list.remove(PropertyChangeListener.class, listener);
      if (list.getListenerCount(DataChangeListener.class) == 0) {
        it.remove();
      }
    }
  }

  @Override
  public void addDesktopListener(DesktopListener l) {
    m_listenerList.add(DesktopListener.class, l);
  }

  @Override
  public void removeDesktopListener(DesktopListener l) {
    m_listenerList.remove(DesktopListener.class, l);
  }

  @Override
  public void addDataChangeListener(DataChangeListener listener, Object... dataTypes) {
    if (dataTypes == null || dataTypes.length == 0) {
      EventListenerList list = m_dataChangeListenerMap.get(null);
      if (list == null) {
        list = new EventListenerList();
        m_dataChangeListenerMap.put(null, list);
      }
      list.add(DataChangeListener.class, listener);
    }
    else {
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_dataChangeListenerMap.get(dataType);
          if (list == null) {
            list = new EventListenerList();
            m_dataChangeListenerMap.put(dataType, list);
          }
          list.add(DataChangeListener.class, listener);
        }
      }
    }
  }

  @Override
  public void removeDataChangeListener(DataChangeListener listener, Object... dataTypes) {
    if (dataTypes == null || dataTypes.length == 0) {
      for (Iterator<EventListenerList> it = m_dataChangeListenerMap.values().iterator(); it.hasNext();) {
        EventListenerList list = it.next();
        list.remove(DataChangeListener.class, listener);
        if (list.getListenerCount(DataChangeListener.class) == 0) {
          it.remove();
        }
      }
    }
    else {
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_dataChangeListenerMap.get(dataType);
          if (list != null) {
            list.remove(DataChangeListener.class, listener);
            if (list.getListenerCount(DataChangeListener.class) == 0) {
              m_dataChangeListenerMap.remove(dataType);
            }
          }
        }
      }
    }
  }

  private UnsupportedOperationException createUnsupportedOperationException() {
    return new UnsupportedOperationException("The desktop is currently loading. This method must be called after the desktop has loaded and is set onto the session");
  }

  /*
   * Not implemented methods
   */

  @Override
  public boolean isTrayVisible() {
    return false;
  }

  @Override
  public void setTrayVisible(boolean b) {
  }

  @Override
  public boolean isShowing(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isAutoPrefixWildcardForTextSearch() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setAutoPrefixWildcardForTextSearch(boolean b) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void activateBookmark(Bookmark bm, boolean forceReload) throws ProcessingException {
    throw createUnsupportedOperationException();
  }

  @Override
  public void addFileChooser(IFileChooser fc) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void openUrlInBrowser(String url) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void openUrlInBrowser(String url, IUrlTarget target) {
    throw createUnsupportedOperationException();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void openBrowserWindow(String path) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void addForm(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void addKeyStrokes(IKeyStroke... keyStrokes) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void addMessageBox(IMessageBox mb) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void closeInternal() throws ProcessingException {
    throw createUnsupportedOperationException();
  }

  @Override
  public Bookmark createBookmark() throws ProcessingException {
    throw createUnsupportedOperationException();
  }

  @Override
  public Bookmark createBookmark(IPage page) throws ProcessingException {
    throw createUnsupportedOperationException();
  }

  @Override
  public void dataChanged(Object... dataTypes) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void ensureViewStackVisible() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void ensureVisible(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IForm> T findForm(Class<T> formType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IForm> T[] findForms(Class<T> formType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IOutline> T findOutline(Class<T> outlineType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IAction> T findAction(Class<T> actionType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IViewButton> T findViewButton(Class<T> viewButtonType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IToolButton> T findToolButton(Class<T> toolButtonType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IForm> T findLastActiveForm(Class<T> formType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IOutline[] getAvailableOutlines() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setAvailableOutlines(IOutline[] availableOutlines) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IForm[] getDialogStack() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IFormField getFocusOwner() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IKeyStroke[] getKeyStrokes() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IMenu[] getMenus() {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IMenu> T getMenu(Class<? extends T> searchType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IMessageBox[] getMessageBoxStack() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IOutline getOutline() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IForm getPageSearchForm() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setPageSearchForm(IForm f) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IForm getPageDetailForm() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setPageDetailForm(IForm f) {
    throw createUnsupportedOperationException();
  }

  @Override
  public ITable getPageDetailTable() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setPageDetailTable(ITable t) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setOutlineTableForm(IOutlineTableForm f) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IOutlineTableForm getOutlineTableForm() {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isOutlineTableFormVisible() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setOutlineTableFormVisible(boolean b) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IForm[] getSimilarViewForms(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IProcessingStatus getStatus() {
    throw createUnsupportedOperationException();
  }

  @Override
  public String getTitle() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IAction[] getActions() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IViewButton[] getViewButtons() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IToolButton[] getToolButtons() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IDesktopUIFacade getUIFacade() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IForm[] getViewStack() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void initDesktop() throws ProcessingException {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isOpened() {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isGuiAvailable() {
    throw createUnsupportedOperationException();

  }

  @Override
  public void prepareAllMenus() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void printDesktop(PrintDevice device, Map<String, Object> parameters) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void refreshPages(Class... pageTypes) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void releaseUnusedPages() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void afterTablePageLoaded(IPageWithTable<?> page) throws ProcessingException {
    throw createUnsupportedOperationException();
  }

  @Override
  public void removeForm(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void removeKeyStrokes(IKeyStroke... keyStrokes) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setKeyStrokes(IKeyStroke[] ks) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setOutline(IOutline outline) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setOutline(Class<? extends IOutline> outlineType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setStatus(IProcessingStatus status) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setStatusText(String s) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setTitle(String s) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void changeVisibilityAfterOfflineSwitch() {
    return;
  }

  @Override
  public boolean doBeforeClosingInternal() {
    throw createUnsupportedOperationException();
  }

}
