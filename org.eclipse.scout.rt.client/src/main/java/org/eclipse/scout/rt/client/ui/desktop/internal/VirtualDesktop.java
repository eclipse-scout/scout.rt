/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.Coordinates;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListeners;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopUIFacade;
import org.eclipse.scout.rt.client.ui.desktop.IOpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchLayoutData;
import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeManager;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.visitor.IBreadthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 * This class is used as a placeholder of virtual desktop while the desktop is loading until the desktop is set onto the
 * {@link IClientSession}. Reasons for that are observer attachments and data change registration in the init block of
 * forms, fields, pages that must be done while the desktop is loading. This pattern solves the bird/egg problem in
 * initialization of an object with self-references.
 */
public class VirtualDesktop implements IDesktop {
  private final DesktopListeners m_listeners;
  private final BasicPropertySupport m_propertyChangeListeners;
  private final IDataChangeManager m_dataChangeListeners;
  private final IDataChangeManager m_dataChangeDesktopInForegroundListeners;

  public VirtualDesktop() {
    m_listeners = new DesktopListeners();
    m_propertyChangeListeners = new BasicPropertySupport(this);
    m_dataChangeListeners = BEANS.get(IDataChangeManager.class);
    m_dataChangeDesktopInForegroundListeners = BEANS.get(IDataChangeManager.class);
  }

  public Map<String, List<PropertyChangeListener>> getPropertyChangeListenerMap() {
    return m_propertyChangeListeners.getSpecificPropertyChangeListeners();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertyChangeListeners.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertyChangeListeners.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertyChangeListeners.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertyChangeListeners.removePropertyChangeListener(propertyName, listener);
  }

  @Override
  public DesktopListeners desktopListeners() {
    return m_listeners;
  }

  @Override
  public IDataChangeManager dataChangeListeners() {
    return m_dataChangeListeners;
  }

  @Override
  public IDataChangeManager dataChangeDesktopInForegroundListeners() {
    return m_dataChangeDesktopInForegroundListeners;
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
  public void fireDataChangeEvent(DataChangeEvent event) {
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

  @Override
  public Set<IKeyStroke> getKeyStrokes() {
    throw createUnsupportedOperationException();
  }

  @Override
  public List<IMenu> getMenus() {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    throw createUnsupportedOperationException();
  }

  @Override
  public IContextMenu getContextMenu() {
    throw createUnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
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
  public List<IDesktopNotification> getNotifications() {
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
  public Collection<IForm> getSelectedViews(IDisplayParent displayParent) {
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
  public List<? extends IWidget> getChildren() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void visit(Consumer<IWidget> visitor) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IWidget> void visit(Consumer<T> visitor, Class<T> type) {
    throw createUnsupportedOperationException();
  }

  @Override
  public TreeVisitResult visit(Function<IWidget, TreeVisitResult> visitor) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IWidget> TreeVisitResult visit(Function<T, TreeVisitResult> visitor, Class<T> type) {
    throw createUnsupportedOperationException();
  }

  @Override
  public TreeVisitResult visit(IDepthFirstTreeVisitor<IWidget> visitor) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IWidget> TreeVisitResult visit(IDepthFirstTreeVisitor<T> visitor, Class<T> type) {
    throw createUnsupportedOperationException();
  }

  @Override
  public <T extends IWidget> TreeVisitResult visit(IBreadthFirstTreeVisitor<T> visitor, Class<T> type) {
    throw createUnsupportedOperationException();
  }

  @Override
  public TreeVisitResult visit(IBreadthFirstTreeVisitor<IWidget> visitor) {
    throw createUnsupportedOperationException();
  }

  @Override
  public void init() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void reinit() {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isInitConfigDone() {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isInitDone() {
    throw createUnsupportedOperationException();
  }

  @Override
  public boolean isDisposeDone() {
    throw createUnsupportedOperationException();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void initDesktop() {
    throw createUnsupportedOperationException();
  }

  @Override
  public void dispose() {
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
  public void refreshPages(List<Class<? extends IPage<?>>> pages) {
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
  public void reloadGui() {
    throw createUnsupportedOperationException();
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
  public BenchLayoutData getBenchLayoutData() {
    return null;
  }

  @Override
  public void setBenchLayoutData(BenchLayoutData layoutData) {
    // NOP
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
  @SuppressWarnings("deprecation")
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

  @Override
  public boolean isInBackground() {
    return false;
  }

  @Override
  public IEventHistory<DesktopEvent> getEventHistory() {
    return null;
  }

  @Override
  public boolean isGeolocationServiceAvailable() {
    return false;
  }

  @Override
  public void setGeolocationServiceAvailable(boolean available) {
    // NOP
  }

  @Override
  public Future<Coordinates> requestGeolocation() {
    throw createUnsupportedOperationException();
  }
}
