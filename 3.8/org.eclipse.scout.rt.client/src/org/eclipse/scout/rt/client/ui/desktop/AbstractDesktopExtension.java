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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * base implementation of {@link IDesktopExtension}
 */
public abstract class AbstractDesktopExtension implements IDesktopExtension {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDesktopExtension.class);

  private IDesktop m_coreDesktop;

  public AbstractDesktopExtension() {
  }

  @Override
  public ContributionCommand initDelegate() throws ProcessingException {
    return execInit();
  }

  @Override
  public ContributionCommand desktopOpenedDelegate() throws ProcessingException {
    return execOpened();
  }

  @Override
  public ContributionCommand desktopClosingDelegate() throws ProcessingException {
    return execClosing();
  }

  @Override
  public ContributionCommand guiAttachedDelegate() throws ProcessingException {
    return execGuiAttached();
  }

  @Override
  public ContributionCommand guiDetachedDelegate() throws ProcessingException {
    return execGuiDetached();
  }

  @Override
  public ContributionCommand outlineChangedDelegate(IOutline oldOutline, IOutline newOutline) throws ProcessingException {
    return execOutlineChanged(oldOutline, newOutline);
  }

  @Override
  public ContributionCommand customFormModificationDelegate(IHolder<IForm> formHolder) throws ProcessingException {
    return execCustomFormModification(formHolder);
  }

  @Override
  public ContributionCommand pageSearchFormChangedDelegate(IForm oldForm, IForm newForm) throws ProcessingException {
    return execPageSearchFormChanged(oldForm, newForm);
  }

  @Override
  public ContributionCommand pageDetailFormChangedDelegate(IForm oldForm, IForm newForm) throws ProcessingException {
    return execPageDetailFormChanged(oldForm, newForm);
  }

  @Override
  public ContributionCommand pageDetailTableChangedDelegate(ITable oldTable, ITable newTable) throws ProcessingException {
    return execPageDetailTableChanged(oldTable, newTable);
  }

  @Override
  public ContributionCommand tablePageLoadedDelegate(IPageWithTable<?> tablePage) throws ProcessingException {
    return execTablePageLoaded(tablePage);
  }

  @Override
  public ContributionCommand addTrayMenusDelegate(List<IMenu> menus) throws ProcessingException {
    return execAddTrayMenus(menus);
  }

  @Override
  public void contributeOutlines(Collection<IOutline> outlines) {
    Class<? extends IOutline>[] array = getConfiguredOutlines();
    if (array == null) {
      return;
    }
    for (Class<? extends IOutline> element : array) {
      try {
        IOutline o = element.newInstance();
        outlines.add(o);
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
    }
  }

  @Override
  public void contributeActions(Collection<IAction> actions) {
    Class<? extends IAction>[] array = getConfiguredActions();
    if (array == null) {
      return;
    }
    for (Class<? extends IAction> element : array) {
      try {
        IAction a = ConfigurationUtility.newInnerInstance(this, element);
        actions.add(a);
      }
      catch (Exception e) {
        LOG.error(null, e);
      }
    }
  }

  @Override
  public IDesktop getCoreDesktop() {
    return m_coreDesktop;
  }

  @Override
  public void setCoreDesktop(IDesktop desktop) {
    m_coreDesktop = desktop;
  }

  /**
   * Called while this desktop extension is initialized.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(10)
  protected ContributionCommand execInit() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /*
   * Runtime
   */

  /**
   * Called after the core desktop was opened and displayed on the GUI.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(12)
  protected ContributionCommand execOpened() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called before the core desktop is being closed.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(15)
  protected ContributionCommand execClosing() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a UI has been attached to the core desktop. The desktop must not necessarily be open.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(20)
  protected ContributionCommand execGuiAttached() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a UI has been detached from the core desktop. The desktop must not necessarily be open.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(25)
  protected ContributionCommand execGuiDetached() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called whenever a new outline has been activated on the core desktop.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @param oldOutline
   *          old outline that was active before
   * @param newOutline
   *          new outline that is active after the change
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(30)
  protected ContributionCommand execOutlineChanged(IOutline oldOutline, IOutline newOutline) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called right before a form is added to the core desktop. This means this method is called
   * before any UI is informed about the new form. The form is provided in a
   * holder. This allows it to prevent the form being added to the desktop (set
   * reference to {@code null}), do some general modifications needed to be done prior UI instantiation,
   * or even replace it with a different instance.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @param formHolder
   *          contains the form that will be added to the core desktop
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  protected ContributionCommand execCustomFormModification(IHolder<IForm> formHolder) {
    return ContributionCommand.Continue;
  }

  /**
   * Called whenever a new page has been activated (selected) on the core desktop.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @param oldForm
   *          is the search form of the old (not selected anymore) page or {@code null}
   * @param newForm
   *          is the search form of the new (selected) page or {@code null}
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @Order(40)
  @ConfigOperation
  protected ContributionCommand execPageSearchFormChanged(IForm oldForm, IForm newForm) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called whenever a new page has been activated (selected) on the core desktop.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @param oldForm
   *          is the detail form of the old (not selected anymore) page or {@code null}
   * @param newForm
   *          is the detail form of the new (selected) page or {@code null}
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @Order(50)
  @ConfigOperation
  protected ContributionCommand execPageDetailFormChanged(IForm oldForm, IForm newForm) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called whenever a new page has been activated (selected) on the core desktop.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @param oldTable
   *          is the table of the old (not selected anymore) table page or {@code null}
   * @param newTable
   *          is the table of the new (selected) table page or {@code null}
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @Order(60)
  @ConfigOperation
  protected ContributionCommand execPageDetailTableChanged(ITable oldTable, ITable newTable) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a table page was loaded or reloaded.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @param tablePage
   *          the table page that has been (re)loaded
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @Order(62)
  @ConfigOperation
  protected ContributionCommand execTablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called while the tray popup is being built. This method may call {@code getMenu(Class)} on the core desktop
   * to find an existing menu on the core desktop by class type.
   * <p>
   * The (potential) menus added to the {@code menus} list will be post processed. {@link IMenu#prepareAction()} is
   * called on each and then checked if the menu is visible.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   * 
   * @param menus
   *          a live list to add menus to the tray
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   * @throws ProcessingException
   */
  @Order(70)
  @ConfigOperation
  protected ContributionCommand execAddTrayMenus(List<IMenu> menus) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Configures the outlines contributed by this desktop extension. These outlines
   * are automatically added to the core desktop which holds this extension.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return an array of outline type tokens
   * @see IOutline
   */
  @ConfigProperty(ConfigProperty.OUTLINES)
  @Order(20)
  @ConfigPropertyValue("null")
  protected Class<? extends IOutline>[] getConfiguredOutlines() {
    return null;
  }

  private Class<? extends IAction>[] getConfiguredActions() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<IAction>[] fca = ConfigurationUtility.filterClasses(dca, IAction.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

}
