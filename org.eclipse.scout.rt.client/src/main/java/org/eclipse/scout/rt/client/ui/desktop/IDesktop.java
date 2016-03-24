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
package org.eclipse.scout.rt.client.ui.desktop;

import java.beans.PropertyChangeListener;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
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
public interface IDesktop extends IPropertyObserver, IDisplayParent {

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
  String PROP_SELECT_VIEW_TABS_KEY_STROKES_ENABLED = "selectViewTabsKeyStrokesEnabled";
  /**
   * {@link Boolean}
   */
  String PROP_SELECT_VIEW_TABS_KEY_STROKE_MODIFIER = "selectViewTabsKeyStrokeModifier";

  /**
   * {@link Boolean}
   */
  String PROP_CACHE_SPLITTER_POSITION = "cacheSplitterPosition";

  String PROP_DESKTOP_STYLE = "desktopStyle";

  String PROP_ACTIVE_FORM = "activeForm";

  String PROP_THEME = "theme";

  String PROP_BROWSER_HISTORY_ENTRY = "browserHistoryEntry";

  String PROP_NAVIGATION_VISIBLE = "navigationVisible";

  String PROP_HEADER_VISIBLE = "headerVisible";

  String PROP_BENCH_VISIBLE = "benchVisible";

  public enum DesktopStyle {
    /**
     * Default desktop style with header, navigation (tree) and bench (forms).
     */
    DEFAULT,

    /**
     * Reduced desktop style. Only bench is visible.
     */
    BENCH
  }

  void initDesktop();

  /**
   * Returns the first {@link Form} which is of the given type or a sub type of the given type.
   */
  <T extends IForm> T findForm(Class<T> formType);

  /**
   * Returns all registered Forms which are of the given type or a sub type of the given type.
   */
  <T extends IForm> List<T> findForms(Class<T> formType);

  /**
   * @return an available outline of this type ({@link #getAvailableOutlines()}
   */
  <T extends IOutline> T findOutline(Class<T> outlineType);

  /**
   * Find a toolButton or a viewButton in the desktop
   */
  <T extends IAction> T findAction(Class<T> actionType);

  /**
   * Convenience alias for {@link #findAction(Class)}
   */
  <T extends IToolButton> T findToolButton(Class<T> toolButtonType);

  /**
   * Convenience alias for {@link #findAction(Class)}
   */
  <T extends IViewButton> T findViewButton(Class<T> viewButtonType);

  /**
   * Returns all registered Views of the same class and with the same exclusive key, except the current Search- or
   * Detail Form.
   */
  List<IForm> getSimilarViewForms(IForm form);

  /**
   * @return the {@link IFormField} that owns the focus
   */
  IFormField getFocusOwner();

  /**
   * fires a ensure visible event for every form in viewStack
   */
  void ensureViewStackVisible();

  /**
   * fires a activate form event
   *
   * @deprecated use {@link #activateForm(IForm)}; will be removed in version 6.1.
   */
  @Deprecated
  void ensureVisible(IForm form);

  /**
   * fires a activate form event
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
   * @return true after desktop was opened and setup in any UI.
   */
  boolean isOpened();

  /**
   * Returns all Forms which are attached to the given {@link IDisplayParent}. The forms returned are ordered as
   * registered.
   */
  List<IForm> getForms(IDisplayParent displayParent);

  /**
   * Returns all {@link IForm}s of the type {@link IForm#DISPLAY_HINT_VIEW}.
   *
   * @deprecated use {@link #getViews()}; will be removed in version 6.1.
   */
  @Deprecated
  List<IForm> getViewStack();

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
   * see {@link #findAllOpenForms(Class, Class, Object)}
   *
   * @return null if no match is found, else the first encountered match
   */
  <F extends IForm, H extends IFormHandler> F findOpenView(Class<? extends F> formClass, Class<? extends H> handlerClass, Object exclusiveKey);

  /**
   * Returns all {@link IForm}s with dialog character:
   * <ul>
   * <li>{@link IForm#DISPLAY_HINT_DIALOG}</li>
   * <li>{@link IForm#DISPLAY_HINT_POPUP_DIALOG}</li>
   * <li>{@link IForm#DISPLAY_HINT_POPUP_WINDOW}</li>
   * </ul>
   *
   * @deprecated use {@link #getDialogs()}; will be removed in version 6.1.
   */
  @Deprecated
  List<IForm> getDialogStack();

  /**
   * Returns all {@link IForm}s with dialog character in the order as registered.
   * <ul>
   * <li>{@link IForm#DISPLAY_HINT_DIALOG}</li>
   * <li>{@link IForm#DISPLAY_HINT_POPUP_DIALOG}</li>
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
   * <li>{@link IForm#DISPLAY_HINT_POPUP_DIALOG}</li>
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
   * Adds the given {@link IForm} to the desktop and notifies attached listeners like the UI.
   *
   * @deprecated use {@link #showForm(IForm)}; will be removed in version 6.1.
   */
  @Deprecated
  void addForm(IForm form);

  /**
   * Attaches the given {@link IForm} to its {@link IDisplayParent} and displays it.
   */
  void showForm(IForm form);

  /**
   * Removes the given {@link IForm} from the Form's {@link IDisplayParent} and hides it. However, the form is not
   * closed, meaning that it can be added anew in order to be displayed.
   *
   * @deprecated use {@link #hideForm(IForm)}; will be removed in version 6.1.
   */
  @Deprecated
  void removeForm(IForm form);

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
   * Returns all displayed message boxes in the order as attached to the desktop.
   *
   * @deprecated use {@link #getMessageBoxes()}; will be removed in version 6.1.
   */
  @Deprecated
  List<IMessageBox> getMessageBoxStack();

  /**
   * Returns all displayed message boxes in the order as registered.
   */
  List<IMessageBox> getMessageBoxes();

  /**
   * Returns all message boxes which are attached to the given {@link IDisplayParent} in the order as registered.
   */
  List<IMessageBox> getMessageBoxes(IDisplayParent displayParent);

  /**
   * Adds the given {@link IMessageBox} to the desktop and notifies attached listeners like the UI.
   *
   * @deprecated use {@link #showMessageBox(IMessageBox)}; will be removed in version 6.1.
   */
  @Deprecated
  void addMessageBox(IMessageBox messageBox);

  /**
   * Attaches the given {@link IMessageBox} to its {@link IDisplayParent} and displays it.
   */
  void showMessageBox(IMessageBox messageBox);

  /**
   * Removes the given {@link IMessageBox} from desktop and notifies attached listeners like the UI. However, the
   * message box is not closed, meaning that it can be added to the display anew in order to be displayed.
   *
   * @deprecated use {@link #hideMessageBox(IMessageBox)}; will be removed in version 6.1.
   */
  @Deprecated
  void removeMessageBox(IMessageBox messageBox);

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
   * set the given outline on desktop
   *
   * @deprecated use {@link #activateOutline(IOutline)}; will be removed in version 6.1.
   */
  @Deprecated
  void setOutline(IOutline outline);

  /**
   * set the currently active outline on the desktop using its type
   */
  void setOutline(Class<? extends IOutline> outlineType);

  /**
   * Call this method to refresh all existing pages in all outlines<br>
   * If currently active page(s) are affected they reload their data, otherwise the pages is simply marked dirty and
   * reloaded on next activation
   */
  void refreshPages(List<Class<? extends IPage>> pages);

  /**
   * @see IDesktop#refreshPages(List)
   * @param pageTypes
   *          Must be classes that implement {@link IPage}.
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
   * add Model Observer
   */
  void addDesktopListener(DesktopListener l);

  /**
   * add Model Observer as last informed listener.
   */
  void addDesktopListenerAtExecutionEnd(DesktopListener l);

  void removeDesktopListener(DesktopListener l);

  /**
   * add Data Change Observer
   */
  void addDataChangeListener(DataChangeListener listener, Object... dataTypes);

  void removeDataChangeListener(DataChangeListener listener, Object... dataTypes);

  /**
   * Call this method to refresh all listeners on that dataTypes.<br>
   * These might include pages, forms, fields etc.<br>
   *
   * @see {@link AbstractForm#execDataChanged(Object...)} {@link AbstractForm#execDataChanged(Object...)}
   *      {@link AbstractFormField#execDataChanged(Object...)} {@link AbstractFormField#execDataChanged(Object...)}
   *      {@link AbstractPage#execDataChanged(Object...)} {@link AbstractPage#execDataChanged(Object...)}
   */
  void dataChanged(Object... dataTypes);

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
   *
   * @param page
   */
  void afterTablePageLoaded(IPageWithTable<?> page);

  /**
   * Unload and release unused pages in all outlines, such as closed and non-selected nodes
   */
  void releaseUnusedPages();

  /**
   * @return the top level menus
   *         <p>
   *         normally presented in the menubar
   */
  List<IMenu> getMenus();

  /**
   * Convenience to find a menu in the desktop, uses {@link ActionFinder}
   */
  <T extends IMenu> T getMenu(Class<? extends T> searchType);

  /**
   * @return all actions including keyStroke, menu, toolButton and viewButton
   */
  List<IAction> getActions();

  /**
   * Convenience to find a toolbutton in the desktop, uses {@link ActionFinder}
   */
  <T extends IToolButton> T getToolButton(Class<? extends T> searchType);

  /**
   * @return all {@link IToolButton} actions
   */
  List<IToolButton> getToolButtons();

  /**
   * Convenience to find a menu in the desktop, uses {@link ActionFinder}
   */
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
   * see {@link #getPageDetailForm()}, {@link AbstractDesktop#execChangedPageDetailForm(IForm)}
   */
  void setPageDetailForm(IForm f);

  /**
   * @return the detail table of the active (selected) page {@link IPage#getDetailTable()} of the active outline
   *         {@link IOutline#getDetailTable()}
   */
  ITable getPageDetailTable();

  /**
   * see {@link #getPageDetailTable()}, {@link AbstractDesktop#execChangedPageDetailTable(IForm)}
   */
  void setPageDetailTable(ITable t);

  /**
   * @return the search form of the active (selected) page {@link IPageWithTable#getSearchFormInternal()} of the active
   *         outline {@link IOutline#getSearchForm()}
   */
  IForm getPageSearchForm();

  /**
   * see {@link #getPageSearchForm()}, {@link AbstractDesktop#execChangedPageSearchForm(IForm)}
   */
  void setPageSearchForm(IForm f);

  String getTitle();

  void setTitle(String s);

  /**
   * @return <code>true</code> if UI key strokes to select view tabs are enabled, <code>false</code> otherwise.
   */
  boolean isSelectViewTabsKeyStrokesEnabled();

  /**
   * @param selectViewTabsKeyStrokesEnabled
   *          <code>true</code> to enable UI key strokes to select view tabs, <code>false</code> to disable them.
   */
  void setSelectViewTabsKeyStrokesEnabled(boolean selectViewTabsKeyStrokesEnabled);

  /**
   * @return optional modifier to use for UI key strokes to select view tabs (only relevant when
   *         {@link #isSelectViewTabsKeyStrokesEnabled()} is <code>true</code>).
   */
  String getSelectViewTabsKeyStrokeModifier();

  /**
   * @param selectViewTabsKeyStrokeModifier
   *          optional modifier to use for UI key strokes to select view tabs (only relevant when
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
   *
   * @param notification
   */
  void removeNotification(IDesktopNotification notification);

  /**
   * @return <code>true</code> if the given {@link IFileChooser} is currently displayed. However, a value of
   *         <code>true</code> does not imply that the {@link IFileChooser} is the currently active model element.
   * @see #showFileChooser(IFileChooser)
   */
  boolean isShowing(IFileChooser fileChooser);

  /**
   * @deprecated use {@link #getFileChoosers()}; will be removed in version 6.1.
   */
  @Deprecated
  List<IFileChooser> getFileChooserStack();

  /**
   * Returns all displayed {@link IFileChooser}s in the order as registered.
   */
  List<IFileChooser> getFileChoosers();

  /**
   * Returns all file choosers which are attached to the given {@link IDisplayParent} in the order as registered.
   */
  List<IFileChooser> getFileChoosers(IDisplayParent displayParent);

  /**
   * Retrieve files via a user interface
   *
   * @deprecated use {@link #showFileChooser(IFileChooser)}; will be removed in version 6.1.
   */
  @Deprecated
  void addFileChooser(IFileChooser fileChooser);

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
   * Downloads the given binary resource. Download handler is valid for 1 minute.
   *
   * @param binaryResource
   *          The binary resource that should be opened on the UI using a temporary URI. Must not be <code>null</code>.
   * @param openUriAction
   *          The action to be performed on the UI for the URI. Must not be <code>null</code>.
   */
  void openUri(BinaryResource binaryResource, IOpenUriAction openUriAction);

  /**
   * Activates a {@link Bookmark} on this desktop.
   * <p />
   * First the specific {@link Bookmark#getOutlineClassName()} is evaluated and activated, afterwards every page from
   * the {@link Bookmark#getPath()} will be selected (respecting the {@link AbstractPageState}).
   * <p />
   * Finally the path will be expanded. Possible exceptions might occur if no outline is set in the {@link Bookmark} or
   * the outline is not available.
   */
  void activateBookmark(Bookmark bm);

  /**
   * Activates a {@link Bookmark} on this desktop.
   * <p />
   * First the specific {@link Bookmark#getOutlineClassName()} is evaluated and, if activateOutline is true, activated.
   * Afterwards every page from the {@link Bookmark#getPath()} will be selected (respecting the
   * {@link AbstractPageState}).
   * <p />
   * Finally the path will be expanded. Possible exceptions might occur if no outline is set in the {@link Bookmark} or
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
   * @return the collection of untyped add-ons in this Desktop
   * @since 5.1.0
   */
  Collection<Object> getAddOns();

  /**
   * Adds an untyped add-on to the Desktop. Add-ons are required when you want to extend your Desktop with something
   * that needs to add elements to the DOM of the user interface. Typically these DOM elements are not visible, so the
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
   * Returns true while outline is changing, which happens when <code>setOutline(IOutline)</code> is called.
   *
   * @return whether or not outline is changing
   * @since 5.1.0
   */
  boolean isOutlineChanging();

  /**
   * @return the desktop style. Default value is <code>DesktopStyle.DEFAULT</code>.
   * @since 5.2.0
   */
  DesktopStyle getDesktopStyle();

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
  void setBenchVisible(boolean visible);

  /**
   * @since 6.0
   */
  boolean isBenchVisible();

  /**
   * @since 6.0
   */
  void setHeaderVisible(boolean visible);

  /**
   * @since 6.0
   */
  boolean isHeaderVisible();

}
