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

import java.beans.PropertyChangeListener;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.extension.ui.desktop.DefaultDesktopEventHistory;
import org.eclipse.scout.rt.client.ui.Coordinates;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.WidgetEvent;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchLayoutData;
import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeListener;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeManager;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.notification.NativeNotificationDefaults;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 * The desktop model (may) consist of
 * <ul>
 * <li>set of available outline
 * <li>active outline
 * <li>active tableview
 * <li>active detail form
 * <li>active search form
 * <li>form stack (swing: dialogs on desktop as JInternalFrames; eclipse: editors or views)
 * <li>dialog stack of model and non-modal dialogs (swing: dialogs as JDialog, JFrame; eclipse: dialogs in a new Shell)
 * <li>active message box stack
 * <li>top-level menus (menu tree)
 * </ul>
 */
public interface IDesktop extends IWidget, IDisplayParent, IStyleable, IContextMenuOwner {

  /**
   * The {@link IDesktop} which is currently associated with the current thread.
   */
  ThreadLocal<IDesktop> CURRENT = new ThreadLocal<>();

  /**
   * String
   */
  String PROP_TITLE = "title";

  /**
   * String
   */
  String PROP_LOGO_ID = "logoId";

  /**
   * {@link IStatus}
   */
  String PROP_STATUS = "status";
  /**
   * {@link List<IKeyStroke>}
   */
  String PROP_KEY_STROKES = "keyStrokes";
  /**
   * Specifies that the Desktop has been completely initialized and started in Java and the Browser has completed the
   * Desktop too. {@link Boolean}
   */
  String PROP_READY = "ready";
  /**
   * Specifies that the Desktop has been completely initialized and started in Java. It is not ready yet in the Browser!
   * {@link Boolean}
   */
  String PROP_OPENED = "opened";
  /**
   * {@link Boolean}
   */
  String PROP_GUI_AVAILABLE = "guiAvailable";
  /**
   * {@link Boolean}
   */
  String PROP_GEOLOCATION_SERVICE_AVAILABLE = "geolocationServiceAvailable";
  /**
   * {@link Boolean}
   */
  String PROP_SELECT_VIEW_TABS_KEY_STROKES_ENABLED = "selectViewTabsKeyStrokesEnabled";
  /**
   * {@link Boolean}
   */
  String PROP_SELECT_VIEW_TABS_KEY_STROKE_MODIFIER = "selectViewTabsKeyStrokeModifier";

  /**
   * {@link Boolean}
   */
  String PROP_CACHE_SPLITTER_POSITION = "cacheSplitterPosition";

  String PROP_DISPLAY_STYLE = "displayStyle";

  String PROP_NATIVE_NOTIFICATION_DEFAULTS = "nativeNotificationDefaults";

  String PROP_ACTIVE_FORM = "activeForm";

  String PROP_FOCUSED_ELEMENT = "focusedElement";

  String PROP_TRACK_FOCUS = "trackFocus";

  String PROP_THEME = "theme";

  String PROP_BROWSER_HISTORY_ENTRY = "browserHistoryEntry";

  String PROP_NAVIGATION_VISIBLE = "navigationVisible";

  String PROP_NAVIGATION_HANDLE_VISIBLE = "navigationHandleVisible";

  String PROP_HEADER_VISIBLE = "headerVisible";

  String PROP_IN_BACKGROUND = "inBackground";

  String PROP_BENCH_VISIBLE = "benchVisible";

  String PROP_BENCH_LAYOUT_DATA = "benchLayoutData";

  String PROP_LOGO_ACTION_ENABLED = "logoActionEnabled";

  String PROP_STARTUP_REQUEST_PARAMS = "startupRequestParams";

  String STARTUP_REQUEST_PARAM_URL = "url";

  String PROP_DENSE = "dense";

  /**
   * Default style with header, navigation (tree) and bench (forms).
   * <p>
   * <code>initDisplayStyle({@link #DISPLAY_STYLE_DEFAULT})</code> will assign the following property values:
   * <ul>
   * <li><i>navigationVisible</i> = <code>getConfiguredNavigationVisible()</code>
   * <li><i>navigationHandleVisible</i> = <code>getConfiguredNavigationHandleVisible()</code>
   * <li><i>headerVisible</i> = <code>getConfiguredHeaderVisible()</code>
   * <li><i>benchVisible</i> = <code>getConfiguredBenchVisible()</code>
   * </ul>
   */
  String DISPLAY_STYLE_DEFAULT = "default";

  /**
   * Reduced style. Only bench is visible.
   * <p>
   * <code>initDisplayStyle({@link #DISPLAY_STYLE_BENCH})</code> will assign the following property values:
   * <ul>
   * <li><i>navigationVisible</i> = <b>false</b>
   * <li><i>navigationHandleVisible</i> = <b>false</b>
   * <li><i>headerVisible</i> = <b>false</b>
   * <li><i>benchVisible</i> = <b>true</b>
   * </ul>
   */
  String DISPLAY_STYLE_BENCH = "bench";

  /**
   * Compact style. Navigation and bench are never visible simultaneously.
   * <p>
   * <code>initDisplayStyle({@link #DISPLAY_STYLE_COMPACT})</code> will assign the following property values:
   * <ul>
   * <li><i>navigationVisible</i> = <b>true</b>
   * <li><i>navigationHandleVisible</i> = <b>false</b>
   * <li><i>headerVisible</i> = <b>false</b>
   * <li><i>benchVisible</i> = <b>false</b>
   * </ul>
   */
  String DISPLAY_STYLE_COMPACT = "compact";

  /**
   * Returns the first {@link Form} which is of the given type or a subtype of the given type.
   */
  <T extends IForm> T findForm(Class<T> formType);

  /**
   * Returns all registered Forms which are of the given type or a subtype of the given type.
   */
  <T extends IForm> List<T> findForms(Class<T> formType);

  /**
   * @return an available outline of this type ({@link #getAvailableOutlines()}
   */
  <T extends IOutline> T findOutline(Class<T> outlineType);

  <T extends IAction> T findAction(Class<T> actionType);

  /**
   * Convenience alias for {@link #findAction(Class)}
   */
  <T extends IViewButton> T findViewButton(Class<T> viewButtonType);

  /**
   * Returns all registered forms of the same class and with the same exclusive key, except the current Search- or
   * Detail Form.
   */
  List<IForm> getSimilarForms(IForm form);

  /**
   * fires an ensure visible event for every form in viewStack
   */
  void ensureViewStackVisible();

  /**
   * fires an activate form event
   */
  void activateForm(IForm form);

  /**
   * sets the given outline on desktop and brings its content to front
   */
  void activateOutline(IOutline outline);

  /**
   * Activates the first visible and enabled page of the current outline.
   */
  void activateFirstPage();

  /**
   * @return <code>true</code> if the given {@link IForm} is currently displayed. However, a value of <code>true</code>
   *         does not imply that the {@link IForm} is the currently active {@link IForm}.
   * @see #showForm(IForm)
   */
  boolean isShowing(IForm form);

  /**
   * @return true after desktop was opened and setup in Java.
   */
  boolean isOpened();

  /**
   * @return true if the desktop was initialized and opened in Java and the UI.
   */
  boolean isReady();

  /**
   * Returns all Forms which are attached to the given {@link IDisplayParent}. The forms returned are ordered as
   * registered.
   */
  List<IForm> getForms(IDisplayParent displayParent);

  /**
   * Returns all displayed Forms of the type {@link IForm#DISPLAY_HINT_VIEW} in the order as registered.
   */
  List<IForm> getViews();

  /**
   * Returns all Forms of the type {@link IForm#DISPLAY_HINT_VIEW} and which are attached to the given
   * {@link IDisplayParent}. The forms returned are ordered as registered.
   */
  List<IForm> getViews(IDisplayParent displayParent);

  /**
   * @return all form selected in view tabs (top level forms)
   */
  Collection<IForm> getSelectedViews(IDisplayParent displayParent);

  /**
   * @param formClass
   *          - class of the form to be searched
   * @param handlerClass
   *          - class of the expected active form handler
   * @param exclusiveKey
   *          - exclusive key of the form, returned by {@link IForm#computeExclusiveKey}
   * @return a list (maybe empty, but never null) of forms that match the above criteria <br/>
   *         <b>note:</b> if either argument is null, an empty list is returned.
   */
  <F extends IForm, H extends IFormHandler> List<F> findAllOpenViews(Class<? extends F> formClass, Class<? extends H> handlerClass, Object exclusiveKey);

  /**
   * see {@link #findAllOpenViews(Class, Class, Object)}
   *
   * @return null if no match is found, else the first encountered match
   */
  <F extends IForm, H extends IFormHandler> F findOpenView(Class<? extends F> formClass, Class<? extends H> handlerClass, Object exclusiveKey);

  /**
   * Returns all {@link IForm}s with dialog character in the order as registered.
   * <ul>
   * <li>{@link IForm#DISPLAY_HINT_DIALOG}</li>
   * <li>{@link IForm#DISPLAY_HINT_POPUP_WINDOW}</li>
   * </ul>
   */
  List<IForm> getDialogs();

  /**
   * Returns all {@link IForm}s with dialog character and which are attached to the given {@link IDisplayParent}. The
   * dialogs returned are ordered as registered.
   * <p>
   * A dialog has one of the following display hint:
   * <ul>
   * <li>{@link IForm#DISPLAY_HINT_DIALOG}</li>
   * <li>{@link IForm#DISPLAY_HINT_POPUP_WINDOW}</li>
   * </ul>
   * <p>
   * Optionally, this method can be parameterized to include all child dialogs recursively. In the resulting list, child
   * dialogs come before their parent dialog, and child dialogs of the same parent in the order as inserted.
   *
   * @param displayParent
   *          'displayParent' like {@link IDesktop}, {@link IOutline} or {@link IForm}.
   * @param includeChildDialogs
   *          <code>true</code> to include child dialogs, or <code>false</code> to only include direct children of the
   *          given {@link IDisplayParent}.
   */
  List<IForm> getDialogs(IDisplayParent displayParent, boolean includeChildDialogs);

  /**
   * Open dialogs or views that need to be saved
   */
  List<IForm> getUnsavedForms();

  /**
   * Attaches the given {@link IForm} to its {@link IDisplayParent} and displays it.
   */
  void showForm(IForm form);

  /**
   * Removes the given {@link IForm} from its {@link IDisplayParent} and hides it. However, the form is not closed,
   * meaning that it can be added anew in order to be displayed. This method has no effect if the {@link IForm} is not
   * showing.
   */
  void hideForm(IForm form);

  /**
   * @return <code>true</code> if the given {@link IMessageBox} is currently displayed. However, a value of
   *         <code>true</code> does not imply that the message box is the currently active message box.
   * @see #showMessageBox(IMessageBox)
   */
  boolean isShowing(IMessageBox messageBox);

  /**
   * Returns all displayed message boxes in the order as registered.
   */
  List<IMessageBox> getMessageBoxes();

  /**
   * Returns all displayed notifications in the same order as registered.
   */
  List<IDesktopNotification> getNotifications();

  /**
   * Returns all message boxes which are attached to the given {@link IDisplayParent} in the order as registered.
   */
  List<IMessageBox> getMessageBoxes(IDisplayParent displayParent);

  /**
   * Attaches the given {@link IMessageBox} to its {@link IDisplayParent} and displays it.
   */
  void showMessageBox(IMessageBox messageBox);

  /**
   * Removes the given {@link IMessageBox} from its {@link IDisplayParent} and hides it. However, the message box is not
   * closed, meaning that it can be added anew in order to be displayed. This method has no effect if the
   * {@link IMessageBox} is not showing.
   */
  void hideMessageBox(IMessageBox messageBox);

  List<IOutline> getAvailableOutlines();

  void setAvailableOutlines(List<? extends IOutline> availableOutlines);

  Set<IKeyStroke> getKeyStrokes();

  void setKeyStrokes(Collection<? extends IKeyStroke> ks);

  void addKeyStrokes(IKeyStroke... keyStrokes);

  void removeKeyStrokes(IKeyStroke... keyStrokes);

  /**
   * @return the currently active outline on the desktop
   */
  IOutline getOutline();

  /**
   * set the currently active outline on the desktop using its type
   */
  void setOutline(Class<? extends IOutline> outlineType);

  /**
   * Call this method to refresh all existing pages in all outlines<br>
   * If currently active page(s) are affected they reload their data, otherwise the pages is simply marked dirty and
   * reloaded on next activation
   */
  void refreshPages(List<Class<? extends IPage<?>>> pages);

  /**
   * @param pageTypes
   *          Must be classes that implement {@link IPage}.
   * @see IDesktop#refreshPages(List)
   */
  void refreshPages(Class<?>... pageTypes);

  /**
   * add Property Observer
   */
  @Override
  void addPropertyChangeListener(PropertyChangeListener listener);

  @Override
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * add Property Observer
   */
  @Override
  void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  @Override
  void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Accessor to the table listener registry
   */
  DesktopListeners desktopListeners();

  /**
   * @param eventTypes
   *          of {@link DesktopEvent} TYPE_*
   */
  default void addDesktopListener(DesktopListener listener, Integer... eventTypes) {
    desktopListeners().add(listener, false, eventTypes);
  }

  default void removeDesktopListener(DesktopListener listener, Integer... eventTypes) {
    desktopListeners().remove(listener, eventTypes);
  }

  /**
   * Add the listener so it is called as <em>last</em> listener.
   * <p>
   * Use {@link #addDesktopListener(DesktopListener, Integer...)} in all other cases
   *
   * @param eventTypes
   *          of {@link DesktopEvent} TYPE_*
   */
  default void addUIDesktopListener(DesktopListener listener, Integer... eventTypes) {
    desktopListeners().addLastCalled(listener, false, eventTypes);
  }

  IDataChangeManager dataChangeListeners();

  IDataChangeManager dataChangeDesktopInForegroundListeners();

  default void addDataChangeListener(IDataChangeListener listener, Object... dataTypes) {
    dataChangeListeners().add(listener, false, dataTypes);
  }

  /**
   * Add Data Change Observer that is notified only if this desktop is in foreground (evaluated by the UI-layer, not
   * controlled by the client model) and if {@link #isDataChanging()} returns {@code false}. Otherwise, data change
   * notifications are deferred until the two conditions are met.
   */
  default void addDataChangeDesktopInForegroundListener(IDataChangeListener listener, Object... dataTypes) {
    dataChangeDesktopInForegroundListeners().add(listener, false, dataTypes);
  }

  default void removeDataChangeListener(IDataChangeListener listener, Object... dataTypes) {
    dataChangeListeners().remove(listener, dataTypes);
    dataChangeDesktopInForegroundListeners().remove(listener, dataTypes);
  }

  /**
   * Call this method to refresh all listeners on that dataTypes.<br>
   * These might include pages, forms, fields etc.<br>
   *
   * @param dataTypes
   *          accepts objects that act as {@link DataChangeEvent#getDataType()} in implicitly created
   *          {@link DataChangeEvent}s.
   * @see AbstractForm#execDataChanged(Object...)
   * @see AbstractForm#execDataChanged(Object...)
   * @see AbstractFormField#execDataChanged(Object...)
   * @see AbstractFormField#execDataChanged(Object...)
   * @see AbstractPage#execDataChanged(Object...)
   * @see AbstractPage#execDataChanged(Object...)
   */
  void dataChanged(Object... dataTypes);

  /**
   * Call this method to refresh all listeners on that {@link DataChangeEvent#getDataType()}.<br>
   * These might include pages, forms, fields etc.<br>
   *
   * @see AbstractForm#execDataChanged(Object...)
   * @see AbstractForm#execDataChanged(Object...)
   * @see AbstractFormField#execDataChanged(Object...)
   * @see AbstractFormField#execDataChanged(Object...)
   * @see AbstractPage#execDataChanged(Object...)
   * @see AbstractPage#execDataChanged(Object...)
   * @since 8.0
   */
  void fireDataChangeEvent(DataChangeEvent event);

  /**
   * marks desktop data as changing and all data changed events are cached until the change is done
   * <p>
   * when done, all cached events are coalesced and sent as a batch
   */
  void setDataChanging(boolean b);

  boolean isDataChanging();

  /**
   * Called after a page was loaded or reloaded.
   * <p>
   * Default minimizes page search form when data was found.
   */
  void afterTablePageLoaded(IPageWithTable<?> page);

  /**
   * Unload and release unused pages in all outlines, such as closed and non-selected nodes
   */
  void releaseUnusedPages();

  /**
   * @return all actions including keyStroke, menu, toolButton and viewButton
   */
  List<IAction> getActions();

  <T extends IViewButton> T getViewButton(Class<? extends T> searchType);

  /**
   * @return all {@link IViewButton} actions
   */
  List<IViewButton> getViewButtons();

  /**
   * @return the detail form of the active (selected) page {@link IPage#getDetailForm()} of the active outline
   *         {@link IOutline#getDetailForm()}
   */
  IForm getPageDetailForm();

  /**
   * see {@link #getPageDetailForm()}, {@link AbstractDesktop#execPageDetailFormChanged(IForm, IForm)}
   */
  void setPageDetailForm(IForm f);

  /**
   * @return the detail table of the active (selected) page {@link IPage#getTable()} of the active outline
   *         {@link IOutline#getDetailTable()}
   */
  ITable getPageDetailTable();

  /**
   * see {@link #getPageDetailTable()}, {@link AbstractDesktop#execPageDetailTableChanged(ITable, ITable)}
   */
  void setPageDetailTable(ITable t);

  /**
   * @return the search form of the active (selected) page {@link IPageWithTable#getSearchFormInternal()} of the active
   *         outline {@link IOutline#getSearchForm()}
   */
  IForm getPageSearchForm();

  /**
   * see {@link #getPageSearchForm()}, {@link AbstractDesktop#execPageSearchFormChanged(IForm, IForm)}
   */
  void setPageSearchForm(IForm f);

  String getTitle();

  void setTitle(String s);

  /**
   * @return <code>true</code> if UI keystrokes to select view tabs are enabled, <code>false</code> otherwise.
   */
  boolean isSelectViewTabsKeyStrokesEnabled();

  /**
   * @param selectViewTabsKeyStrokesEnabled
   *          <code>true</code> to enable UI keystrokes to select view tabs, <code>false</code> to disable them.
   */
  void setSelectViewTabsKeyStrokesEnabled(boolean selectViewTabsKeyStrokesEnabled);

  /**
   * @return optional modifier to use for UI keystrokes to select view tabs (only relevant when
   *         {@link #isSelectViewTabsKeyStrokesEnabled()} is <code>true</code>).
   */
  String getSelectViewTabsKeyStrokeModifier();

  /**
   * @param selectViewTabsKeyStrokeModifier
   *          optional modifier to use for UI keystrokes to select view tabs (only relevant when
   *          {@link #isSelectViewTabsKeyStrokesEnabled()} is <code>true</code>).
   */
  void setSelectViewTabsKeyStrokeModifier(String selectViewTabsKeyStrokeModifier);

  /**
   * @return true: automatically prefix a * on any text field's search value
   */
  boolean isAutoPrefixWildcardForTextSearch();

  void setAutoPrefixWildcardForTextSearch(boolean b);

  boolean isCacheSplitterPosition();

  void setCacheSplitterPosition(boolean b);

  /**
   * Adds a notification to the desktop.
   *
   * @since 5.2
   */
  void addNotification(IDesktopNotification notification);

  /**
   * Removes a notification from the desktop.
   */
  void removeNotification(IDesktopNotification notification);

  /**
   * @return <code>true</code> if the given {@link IFileChooser} is currently displayed. However, a value of
   *         <code>true</code> does not imply that the {@link IFileChooser} is the currently active model element.
   * @see #showFileChooser(IFileChooser)
   */
  boolean isShowing(IFileChooser fileChooser);

  /**
   * Returns all displayed {@link IFileChooser}s in the order as registered.
   */
  List<IFileChooser> getFileChoosers();

  /**
   * Returns all file choosers which are attached to the given {@link IDisplayParent} in the order as registered.
   */
  List<IFileChooser> getFileChoosers(IDisplayParent displayParent);

  /**
   * Attaches the given {@link IFileChooser} to its {@link IDisplayParent} and displays it.
   */
  void showFileChooser(IFileChooser fileChooser);

  /**
   * Removes the given {@link IFileChooser} from its {@link IDisplayParent} and hides it. However, the file chooser is
   * not closed, meaning that it can be added anew in order to be displayed. This method has no effect if the file
   * chooser is not showing.
   */
  void hideFileChooser(IFileChooser fileChooser);

  /**
   * Opens the given URI (http:, tel:, mailto:, etc.).
   *
   * @param uri
   *          URI to handle on the UI using the given action. Must not be <code>null</code>.
   * @param openUriAction
   *          The action to be performed on the UI for the URI. Must not be <code>null</code>.
   */
  void openUri(String uri, IOpenUriAction openUriAction);

  /**
   * Decides based on the given {@link BinaryResource} which {@link IOpenUriAction} should be used.<br>
   * Downloads the given {@link BinaryResource}. Download handler is valid for 1 minute.
   *
   * @param binaryResource
   *          The binary resource that should be opened on the UI using a temporary URI. Must not be <code>null</code>.
   */
  void openUri(BinaryResource binaryResource);

  /**
   * Downloads the given {@link BinaryResource}. Download handler is valid for 1 minute.
   *
   * @param binaryResource
   *          The binary resource that should be opened on the UI using a temporary URI. Must not be <code>null</code>.
   * @param openUriAction
   *          The action to be performed on the UI for the URI. Must not be <code>null</code>.
   */
  void openUri(BinaryResource binaryResource, IOpenUriAction openUriAction);

  /**
   * Executes the logo action.
   */
  void doLogoAction();

  /**
   * Activates a {@link Bookmark} on this desktop.
   * <p>
   * First the specific {@link Bookmark#getOutlineClassName()} is evaluated and activated. Afterward, every page from
   * the {@link Bookmark#getPath()} will be selected (respecting the {@link AbstractPageState}).
   * <p>
   * Finally, the path will be expanded. Possible exceptions might occur if no outline is set in the {@link Bookmark} or
   * the outline is not available.
   */
  void activateBookmark(Bookmark bm);

  /**
   * Activates a {@link Bookmark} on this desktop.
   * <p>
   * First the specific {@link Bookmark#getOutlineClassName()} is evaluated and, if activateOutline is true, activated.
   * Afterward, every page from the {@link Bookmark#getPath()} will be selected (respecting the
   * {@link AbstractPageState}).
   * <p>
   * Finally, the path will be expanded. Possible exceptions might occur if no outline is set in the {@link Bookmark} or
   * the outline is not available.
   */
  void activateBookmark(Bookmark bm, boolean activateOutline);

  /**
   * Creates a bookmark of the active page
   */
  Bookmark createBookmark();

  /**
   * Creates a bookmark of the given page
   *
   * @since 3.8.0
   */
  Bookmark createBookmark(IPage<?> page);

  /**
   * do not use this internal method.
   * <p>
   * For closing scout see {@link IClientSession#stop()}.
   */
  void closeInternal();

  IDesktopUIFacade getUIFacade();

  boolean isGuiAvailable();

  /**
   * Triggers a reload of the current UI.
   *
   * @since 6.1
   */
  void reloadGui();

  /**
   * This method is used internally within the framework.
   * <p>
   * Called before the desktop gets into its closing state, i.e. the desktop just received a request to close itself.
   * This pre-hook of the closing process adds the possibility to execute some custom code and to abort the closing
   * process.
   * <p>
   * Subclasses can override this method.
   *
   * @return <code>true</code> to allow the desktop to proceed with closing. Otherwise <code>false</code> to veto the
   *         closing process.
   */
  boolean doBeforeClosingInternal();

  /**
   * Gets the currently active (focused) {@link IForm}.
   *
   * @return The currently active {@link IForm} or <code>null</code> if no {@link IForm} is active.
   * @since 4.2.0
   */
  IForm getActiveForm();

  /**
   * @return the currently focused element as long as {@link #isTrackFocus()} is set to true.
   */
  IWidget getFocusedElement();

  /**
   * Set to true to enable focus tracking so that {@link #getFocusedElement()} will return the focused element and the
   * widget will be notified when it gains or looses the focus.<br>
   * Remember to disable the tracking when you don't need it anymore since it will create a http request on each focus
   * change. <br>
   * <b>Important</b>: do not call the function multiple times with the same parameter, because internally a counter is
   * used instead of a boolean to ensure the state is not accidentally changed by another widget.
   *
   * @param trackFocus
   *          true to enable the focus tracking, false to disable it.
   * @see WidgetEvent#TYPE_FOCUS_IN
   * @see WidgetEvent#TYPE_FOCUS_OUT
   */
  void setTrackFocus(boolean trackFocus);

  boolean isTrackFocus();

  /**
   * @return the collection of untyped add-ons in this Desktop
   * @since 5.1.0
   */
  Collection<Object> getAddOns();

  /**
   * Returns the add-on instance or <code>null</code> if add-on is not available. If multiple add-ons match the given
   * class any one is returned (nondeterministic, see {@link java.util.stream.Stream#findAny}).
   *
   * @param addOnClass
   *          The add-on class or a super-class/interface of the add-on
   * @return The add-on instance
   * @since 9.0
   */
  <T> T getAddOn(Class<T> addOnClass);

  /**
   * Adds an untyped add-on to the Desktop. Add-ons are required when you want to extend your Desktop with something
   * that needs to add elements to the DOM of the user interface. Typically, these DOM elements are not visible, so the
   * add-ons are not meant for clickable UI-elements but rather for technical features, like interfaces built with
   * browser-technologies.
   * <p>
   * In order to render the add-ons in the UI you must extend your <code>JsonObjectFactory</code> and provide a
   * <i>Json[AddOn].java</i> and an <i>[AddOn].js</i>.
   *
   * @since 5.1.0
   */
  void addAddOn(Object addOn);

  /**
   * Removes an add-on. Does not have an effect when the Desktop is already rendered.
   */
  void removeAddOn(Object addOn);

  /**
   * Returns true while outline is changing, which happens when <code>setOutline(IOutline)</code> is called.
   *
   * @return whether the outline is changing
   * @since 5.1.0
   */
  boolean isOutlineChanging();

  /**
   * @return the display style. Default value is <code>DISPLAY_STYLE_DEFAULT</code>.
   * @since 5.2.0
   */
  String getDisplayStyle();

  void setDisplayStyle(String displayStyle);

  /***
   * @since 6.0
   */
  String getLogoId();

  /**
   * @since 6.0
   */
  void setLogoId(String id);

  /**
   * @return the name of the current theme or null when default theme is active.
   * @since 5.2.0
   */
  String getTheme();

  /**
   * @param theme
   *          name of the theme to activate or null when default theme should be active.
   * @since 5.2.0
   */
  void setTheme(String theme);

  /**
   * @return the current browser history item
   * @since 6.0
   */
  BrowserHistoryEntry getBrowserHistoryEntry();

  /**
   * Sets the browser history item. Calling this method will change the URL in the address bar of the browser.
   *
   * @since 6.0
   */
  void setBrowserHistoryEntry(BrowserHistoryEntry browserHistory);

  /**
   * @since 6.0
   */
  void setNavigationVisible(boolean visible);

  /**
   * @since 6.0
   */
  boolean isNavigationVisible();

  /**
   * @since 6.0
   */
  void setNavigationHandleVisible(boolean visible);

  /**
   * @since 6.0
   */
  boolean isNavigationHandleVisible();

  /**
   * @since 6.0
   */
  void setBenchVisible(boolean visible);

  /**
   * @since 6.0
   */
  boolean isBenchVisible();

  BenchLayoutData getBenchLayoutData();

  void setBenchLayoutData(BenchLayoutData data);

  /**
   * @since 6.0
   */
  void setHeaderVisible(boolean visible);

  /**
   * @since 6.0
   */
  boolean isHeaderVisible();

  /**
   * @return {@code true} if the desktop in the UI (i.e. web browser) is in the background. <b>Note:</b> The outline
   *         tree may still be visible.
   * @since 6.1
   */
  boolean isInBackground();

  /**
   * @return the {@link IEventHistory} associated with this desktop (might be <code>null</code>).
   *         <p>
   *         The default implementation is a {@link DefaultDesktopEventHistory} and created by
   *         {@link AbstractDesktop#createEventHistory()}
   *         <p>
   *         This method is thread safe.
   * @since 6.0
   */
  IEventHistory<DesktopEvent> getEventHistory();

  /**
   * @since 6.1
   */
  boolean isGeolocationServiceAvailable();

  /**
   * @since 6.1
   */
  void setGeolocationServiceAvailable(boolean available);

  /**
   * @since 6.1
   */
  Future<Coordinates> requestGeolocation();

  /**
   * @since 8.0
   */
  void setLogoActionEnabled(boolean logoActionEnabled);

  /**
   * @since 8.0
   */
  boolean isLogoActionEnabled();

  /**
   * Returns request or URL parameters available at the time the Scout session/desktop has been attached. This map will
   * be available after the desktop has been created but before the openFromUI and attachedFromUI are called.
   * <p>
   * Note: you still need to access <code>PropertyMap.CURRENT.get()</code> when you need a startup parameter in the
   * constructor of the desktop or in a method that is called by <code>initConfig</code>.
   *
   * @return map with request or URL startup parameters
   */
  PropertyMap getStartupRequestParams();

  <VALUE> VALUE getStartupRequestParam(String propertyName);

  /**
   * @return the URL where this Scout application runs (as seen by the browser, including all proxies, etc.).
   */
  String getStartupUrl();

  /**
   * Cancels all forms contained in the given {@link Set} including all open dialogs, views, message boxes and file
   * choosers related to the forms in the formSet (display parent hierarchy). For forms with unsaved changes the user
   * will be asked if they should be saved or not with a specific dialog (see {@link UnsavedFormChangesForm}). If only
   * one form with unsaved changes the regular confirmation message box is shown instead of the
   * {@link UnsavedFormChangesForm}.
   *
   * @param formSet
   *          {@link Set} of {@link IForm}s that should be canceled. Can be null or empty.
   * @return <code>true</code> if all forms were closed (and saved) successfully or false if the saving dialog was
   *         cancelled.
   * @since 9.0
   */
  boolean cancelForms(Set<IForm> formSet);

  /**
   * Cancels all forms contained in the given {@link Set} including all open dialogs, views, message boxes and file
   * choosers related to the forms in the formSet (display parent hierarchy). For forms with unsaved changes the user
   * will be asked if they should be saved or not with a specific dialog (see {@link UnsavedFormChangesForm}).
   *
   * @param formSet
   *          {@link Set} of {@link IForm}s that should be canceled. Can be null or empty.
   * @param alwaysShowUnsavedChangesForm
   *          true, if the {@link UnsavedFormChangesForm} should be shown even if there is only one form to be saved,
   *          false, if the regular confirmation message box should be shown in that case
   * @return <code>true</code> if all forms were closed (and saved) successfully or false if the saving dialog was
   *         cancelled.
   * @since 9.0
   */
  boolean cancelForms(Set<IForm> formSet, boolean alwaysShowUnsavedChangesForm);

  /**
   * Closes all forms contained in the given {@link Set} including all open dialogs, views, message boxes and file
   * choosers related to the forms in the formSet (display parent hierarchy). Compared to {@link #cancelForms(Set)} the
   * user won't be asked to save unsaved changes.
   *
   * @param formSet
   *          {@link Set} of {@link IForm}s that should be closed.
   * @since 9.0
   */
  void closeForms(Set<IForm> formSet);

  /**
   * @since 9.0
   */
  void setDense(boolean dense);

  /**
   * @since 9.0
   */
  boolean isDense();

  /**
   * @since 22.0
   */
  void setNativeNotificationDefaults(NativeNotificationDefaults notificationDefaults);

  /**
   * @since 22.0
   */
  NativeNotificationDefaults getNativeNotificationDefaults();

  /**
   * Reloads a page, and the pages leading to it, starting at its outline root. This method is called when page data
   * changes (see {@link AbstractPage#execDataChanged(Object...)}) and is supposed to make sure that the outline does
   * not display pages that do no longer exist, e.g. if the underlying entity got deleted.
   *
   * @since 22.0
   */
  void reloadPageFromRoot(IPage<?> page);
}
