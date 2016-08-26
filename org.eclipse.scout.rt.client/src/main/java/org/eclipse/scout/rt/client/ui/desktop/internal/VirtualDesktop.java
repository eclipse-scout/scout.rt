/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopUIFacade;
import org.eclipse.scout.rt.client.ui.desktop.IOpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 *  Copyright (c) 2001,2008 BSI AG
 * @version 3.x
 */

/**
 * This class is used as a placeholder of virtual desktop while the desktop is loading until the desktop is set onto the
 * {@link IClientSession}. Reasons for that are observer attachments and data change registration in the init block of
 * forms, fields, pages that must be done while the desktop is loading. This pattern solves the bird/egg problem in
 * initialization of an object with self-references.
 */
public class VirtualDesktop implements IDesktop {

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
  public void addDesktopListenerAtExecutionEnd(DesktopListener l) {
    m_listenerList.insertAtFront(DesktopListener.class, l);
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
        list.removeAll(DataChangeListener.class, listener);
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
  public boolean isAutoPrefixWildcardForTextSearch() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setAutoPrefixWildcardForTextSearch(boolean b) {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isSelectViewTabsKeyStrokesEnabled() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setSelectViewTabsKeyStrokesEnabled(boolean selectViewTabsKeyStrokesEnabled) {
    throw createUnsupportedOperationException();
  }

  @Override
  public String getSelectViewTabsKeyStrokeModifier() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setSelectViewTabsKeyStrokeModifier(String selectViewTabsKeyStrokeModifier) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void activateBookmark(Bookmark bm) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void activateBookmark(Bookmark bm, boolean activateOutline) {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isShowing(IFileChooser fileChooser) {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IFileChooser> getFileChoosers() {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IFileChooser> getFileChoosers(IDisplayParent displayParent) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void showFileChooser(IFileChooser fileChooser) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void hideFileChooser(IFileChooser fileChooser) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void openUri(String url, IOpenUriAction openUriAction) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void openUri(BinaryResource binaryResource, IOpenUriAction openUriAction) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void showForm(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void hideForm(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void addKeyStrokes(IKeyStroke... keyStrokes) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void showMessageBox(IMessageBox messageBox) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void hideMessageBox(IMessageBox messageBox) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void closeInternal() {
    throw createUnsupportedOperationException();
  }

  @Override
  public Bookmark createBookmark() {
    throw createUnsupportedOperationException();
  }

  @Override
  public Bookmark createBookmark(IPage<?> page) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void dataChanged(Object... dataTypes) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setDataChanging(boolean b) {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isDataChanging() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void ensureViewStackVisible() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void activateForm(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void activateOutline(IOutline outline) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void activateFirstPage() {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IForm> T findForm(Class<T> formType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IForm> List<T> findForms(Class<T> formType) {
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
  public List<IOutline> getAvailableOutlines() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setAvailableOutlines(List<? extends IOutline> availableOutlines) {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IForm> getDialogs() {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IForm> getDialogs(IDisplayParent displayParent, boolean includeChildDialogs) {
    throw createUnsupportedOperationException();
  }

  /**
   * this feature isn't supported in html ui
   *
   * @deprecated will be removed in o-release
   */
  @Override
  @Deprecated
  public IFormField getFocusOwner() {
    throw createUnsupportedOperationException();
  }

  @Override
  public Set<IKeyStroke> getKeyStrokes() {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IMenu> getMenus() {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IMenu> T getMenu(Class<? extends T> searchType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isShowing(IMessageBox messageBox) {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IMessageBox> getMessageBoxes() {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IMessageBox> getMessageBoxes(IDisplayParent displayParent) {
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
  public List<IForm> getSimilarViewForms(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public String getTitle() {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IAction> getActions() {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IViewButton> T getViewButton(Class<? extends T> searchType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IViewButton> getViewButtons() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IDesktopUIFacade getUIFacade() {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isShowing(IForm form) {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IForm> getForms(IDisplayParent displayParent) {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IForm> getViews() {
    throw createUnsupportedOperationException();
  }

  @Override
  public <F extends IForm, H extends IFormHandler> List<F> findAllOpenViews(Class<? extends F> formClass, Class<? extends H> handlerClass, Object exclusiveKey) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <F extends IForm, H extends IFormHandler> F findOpenView(Class<? extends F> formClass, Class<? extends H> handlerClass, Object exclusiveKey) {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IForm> getViews(IDisplayParent displayParent) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void initDesktop() {
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
  public void refreshPages(List<Class<? extends IPage>> pages) {
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
  public void afterTablePageLoaded(IPageWithTable<?> page) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void removeKeyStrokes(IKeyStroke... keyStrokes) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setKeyStrokes(Collection<? extends IKeyStroke> ks) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setOutline(Class<? extends IOutline> outlineType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void addNotification(IDesktopNotification notification) {
    // desktop notifications are silently ignored
  }

  @Override
  public void removeNotification(IDesktopNotification notification) {
    // desktop notifications are silently ignored
  }

  @Override
  public void setTitle(String s) {
    throw createUnsupportedOperationException();
  }

  @Override
  public String getCssClass() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void setCssClass(String cssClass) {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean doBeforeClosingInternal() {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IForm> getUnsavedForms() {
    throw createUnsupportedOperationException();
  }

  @Override
  public IForm getActiveForm() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void addAddOn(Object addOn) {
    throw createUnsupportedOperationException();
  }

  @Override
  public Collection<Object> getAddOns() {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isOutlineChanging() {
    throw createUnsupportedOperationException();
  }

  @Override
  public String getDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  @Override
  public void setDisplayStyle(String displayStyle) {
    // always DISPLAY_STYLE_DEFAULT is used
  }

  @Override
  public String getLogoId() {
    return null;
  }

  @Override
  public void setLogoId(String id) {
    // NOP
  }

  @Override
  public boolean isCacheSplitterPosition() {
    return false;
  }

  @Override
  public void setCacheSplitterPosition(boolean b) {
    // NOP
  }

  @Override
  public String getTheme() {
    return null;
  }

  @Override
  public void setTheme(String theme) {
    // NOP
  }

  @Override
  public BrowserHistoryEntry getBrowserHistoryEntry() {
    return null;
  }

  @Override
  public void setBrowserHistoryEntry(BrowserHistoryEntry browserHistory) {
    // NOP
  }

  @Override
  public void setNavigationVisible(boolean visible) {
    // NOP
  }

  @Override
  public boolean isNavigationVisible() {
    return false;
  }

  @Override
  public void setBenchVisible(boolean visible) {
    // NOP
  }

  @Override
  public boolean isBenchVisible() {
    return false;
  }

  @Override
  public void setHeaderVisible(boolean visible) {
    // NOP
  }

  @Override
  public boolean isHeaderVisible() {
    return false;
  }

  @Override
  public <T extends IMenu> T findMenu(Class<T> menuType) {
    return null;
  }

  @Override
  public void setNavigationHandleVisible(boolean visible) {
    // NOP
  }

  @Override
  public boolean isNavigationHandleVisible() {
    return false;
  }

}
