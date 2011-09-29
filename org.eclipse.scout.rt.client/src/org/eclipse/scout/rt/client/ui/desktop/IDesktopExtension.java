package org.eclipse.scout.rt.client.ui.desktop;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * The desktop extension con contribute to a core {@link IDesktop} and react on desktop state changes using the exec*
 * methods in {@link AbstractDesktopExtension}
 * <ul>
 * <li>outlines (with pages)</li>
 * <li>actions (menu, keyStroke, toolButton, viewButton)</li>
 * </ul>
 */
public interface IDesktopExtension {

  /**
   * @return the desktop that holds this extension
   */
  IDesktop getCoreDesktop();

  /**
   * set the desktop that holds this extension
   */
  void setCoreDesktop(IDesktop desktop);

  /**
   * Called while desktop is constructed.
   */
  ContributionCommand initDelegate() throws ProcessingException;

  /**
   * Called after desktop was opened and setup in UI.
   */
  ContributionCommand desktopOpenedDelegate() throws ProcessingException;

  /**
   * Called before the desktop is being closed. May be vetoed using a {@link VetoException}
   */
  ContributionCommand desktopClosingDelegate() throws ProcessingException;

  /**
   * Called after a UI is attached. The desktop must not be necessarily be open.
   */
  ContributionCommand guiAttachedDelegate() throws ProcessingException;

  /**
   * Called after a UI is detached. The desktop must not be necessarily be open.
   */
  ContributionCommand guiDetachedDelegate() throws ProcessingException;

  /**
   * Called whenever a new outline is activated on the desktop.
   */
  ContributionCommand outlineChangedDelegate(IOutline oldOutline, IOutline newOutline) throws ProcessingException;

  /**
   * Called after an other page was selected.
   * 
   * @param oldForm
   *          is the search form of the old (not selected anymore) page or null
   * @param newForm
   *          is the search form of the new (selected) page or null
   */
  ContributionCommand pageSearchFormChangedDelegate(IForm oldForm, IForm newForm) throws ProcessingException;

  /**
   * Called after an other page was selected.
   * 
   * @param oldForm
   *          is the detail form of the old (not selected anymore) page or null
   * @param newForm
   *          is the detail form of the new (selected) page or null
   */
  ContributionCommand pageDetailFormChangedDelegate(IForm oldForm, IForm newForm) throws ProcessingException;

  /**
   * Called after an other page was selected.
   * 
   * @param oldForm
   *          is the table of the old (not selected anymore) table page or null
   * @param newForm
   *          is the table of the new (selected) table page or null
   */
  ContributionCommand pageDetailTableChangedDelegate(ITable oldTable, ITable newTable) throws ProcessingException;

  /**
   * Called after a page was loaded or reloaded.
   * <p>
   * Default minimizes page search form when data was found.
   * 
   * @param page
   */
  ContributionCommand tablePageLoadedDelegate(IPageWithTable<?> tablePage) throws ProcessingException;

  /**
   * Invoked when the tray popup is being built.
   * <p>
   * May use {@link #getMenu(Class)} to find an existing menu in the desktop by class type.
   * <p>
   * The (potential) menus added to the list will be post processed. {@link IMenu#prepareAction()} is called on each and
   * the checked if the menu is visible.
   */
  ContributionCommand addTrayMenusDelegate(List<IMenu> menus) throws ProcessingException;

  /**
   * This is the life list of contributed outlines. They are NOT yet initialized.
   * <p>
   * Use the {@link Order} annotation set the sort order
   */
  void contributeOutlines(Collection<IOutline> outlines);

  /**
   * This is the life list of contributed actions ( {@link IMenu}, {@link IKeyStroke}, {@link IToolButton},
   * {@link IViewButton}). They are NOT yet
   * initialized.
   * <p>
   * Use the {@link Order} annotation set the sort order
   */
  void contributeActions(Collection<IAction> actions);

}
