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

import java.beans.PropertyChangeListener;
import java.util.Map;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
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
public interface IDesktop extends IPropertyObserver {
  /**
   * String
   */
  String PROP_TITLE = "title";
  /**
   * {@link IProcessingStatus}
   */
  String PROP_STATUS = "status";
  /**
   * {@link IKeyStroke}[]
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

  void initDesktop() throws ProcessingException;

  <T extends IForm> T findForm(Class<T> formType);

  <T extends IForm> T[] findForms(Class<T> formType);

  <T extends IForm> T findLastActiveForm(Class<T> formType);

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

  boolean isTrayVisible();

  void setTrayVisible(boolean b);

  /**
   * @param form
   * @return all forms except the searchform and the current detail form with
   *         the same fully qualified classname and the same primary key.
   */
  IForm[] getSimilarViewForms(IForm form);

  /**
   * @return the {@link IFormField} that owns the focus
   */
  IFormField getFocusOwner();

  /**
   * fires a ensure visible event for every form in viewStack
   */
  void ensureViewStackVisible();

  /**
   * fires a ensure visible event
   * 
   * @param form
   */
  void ensureVisible(IForm form);

  /**
   * DISPLAY_HINT_VIEW
   */
  IForm[] getViewStack();

  /**
   * DISPLAY_HINT_DIALOG
   */
  IForm[] getDialogStack();

  /**
   * add form to desktop and notify attached listeners (incl. gui)
   */
  void addForm(IForm form);

  /**
   * remove form from desktop and notify attached listeners (incl. gui)
   */
  void removeForm(IForm form);

  IMessageBox[] getMessageBoxStack();

  void addMessageBox(IMessageBox mb);

  IOutline[] getAvailableOutlines();

  void setAvailableOutlines(IOutline[] availableOutlines);

  IKeyStroke[] getKeyStrokes();

  void setKeyStrokes(IKeyStroke[] ks);

  void addKeyStrokes(IKeyStroke... keyStrokes);

  void removeKeyStrokes(IKeyStroke... keyStrokes);

  /**
   * @return true if the form is currently attached to the desktop, false if the
   *         form is not attached to the desktop<br>
   *         This method can be used to determine if a possibly active form
   *         (started with a running form handler) is currently showing on the
   *         desktop.
   */
  boolean isShowing(IForm form);

  /**
   * @return true after desktop was opened and setup in any UI.
   */
  boolean isOpened();

  /**
   * @return the currently active outline on the desktop
   */
  IOutline getOutline();

  /**
   * set the currently active outline on the desktop
   */
  void setOutline(IOutline outline);

  /**
   * set the currently active outline on the desktop using its type
   */
  void setOutline(Class<? extends IOutline> outlineType);

  /**
   * Call this method to refresh all existing pages in all outlines<br>
   * If currently active page(s) are affected they reload their data, otherwise
   * the pages is simply marked dirty and reloaded on next activation
   */
  void refreshPages(Class... pageTypes);

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
   * Called after a page was loaded or reloaded.
   * <p>
   * Default minimizes page search form when data was found.
   * 
   * @param page
   */
  void afterTablePageLoaded(IPageWithTable<?> page) throws ProcessingException;

  /**
   * Unload and release unused pages in all outlines, such as closed and
   * non-selected nodes
   */
  void releaseUnusedPages();

  /**
   * @return the top level menus
   *         <p>
   *         normally presented in the menubar
   */
  IMenu[] getMenus();

  /**
   * Convenience to find a menu in the desktop, uses {@link ActionFinder}
   */
  <T extends IMenu> T getMenu(Class<? extends T> searchType);

  /**
   * Prepare all (menubar) menus on the desktop.<br>
   * Calls {@link AbstractMenu#execPrepareAction()} on every menu in the menu
   * tree recursively
   */
  void prepareAllMenus();

  /**
   * @return all actions including tool buttons and view buttons
   *         normally these are the tool buttons
   */
  IAction[] getActions();

  /**
   * Convenience for {@link #getActions()} filtered by {@link IToolButton} normally these are the tool buttons
   */
  IToolButton[] getToolButtons();

  /**
   * Convenience for {@link #getActions()} filtered by {@link IViewButton} normally these are the view tabs
   */
  IViewButton[] getViewButtons();

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
   * @return the form that displays the table of the (selected) page {@link IPage#getTable()} of the active outline
   *         {@link IOutline#getDetailTable()}<br>
   * @see {@link #isOutlineTableFormVisible()}
   */
  IOutlineTableForm getOutlineTableForm();

  /**
   * set the detail table form of the active (selected) page {@link IPage#getTable()} of the active outline
   * {@link IOutline#getDetailTable()}
   * 
   * @see {@link #setOutlineTableFormVisible(boolean)}
   */
  void setOutlineTableForm(IOutlineTableForm f);

  /**
   * @return true if the outline table form is visible
   */
  boolean isOutlineTableFormVisible();

  /**
   * set the detail table form of the active (selected) page {@link IPage#getTable()} of the active outline
   * {@link IOutline#getDetailTable()}
   */
  void setOutlineTableFormVisible(boolean b);

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
   * @return true: automatically prefix a * on any text field's search value
   */
  boolean isAutoPrefixWildcardForTextSearch();

  void setAutoPrefixWildcardForTextSearch(boolean b);

  /**
   * get the status of the desktop
   * <p>
   * see also {@link IForm#getFormStatus(IProcessingStatus)}
   */
  IProcessingStatus getStatus();

  /**
   * set a status on the desktop
   * <p>
   * this is normally displayed in as a tray message box
   * <p>
   * see also {@link IForm#setFormStatus(IProcessingStatus)}
   */
  void setStatus(IProcessingStatus status);

  /**
   * set a status on the desktop
   * <p>
   * this is normally displayed in as a tray message box
   * <p>
   * see also {@link IForm#setFormStatusText(String)}
   */
  void setStatusText(String s);

  /**
   * Retrieve files via a user interface
   */
  void addFileChooser(IFileChooser fc);

  /**
   * Prints the desktop parameter details see {@link PrintDevice}
   */
  void printDesktop(PrintDevice device, Map<String, Object> parameters);

  /**
   * activate a bookmark
   */
  void activateBookmark(Bookmark bm, boolean forceReload) throws ProcessingException;

  /**
   * activate a bookmark
   */
  Bookmark createBookmark() throws ProcessingException;

  /**
   * do not use this internal method.<br>
   * for closing scout see <code>ClientScoutSession.getSession().close()</code>
   */
  void closeInternal() throws ProcessingException;

  IDesktopUIFacade getUIFacade();

  boolean isGuiAvailable();

  void changeVisibilityAfterOfflineSwitch();
}
