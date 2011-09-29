/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
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
import org.eclipse.scout.commons.exception.VetoException;
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
   * Called while desktop is constructed.
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
   * Called after desktop was opened and setup in UI.
   */
  @ConfigOperation
  @Order(12)
  protected ContributionCommand execOpened() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called before the desktop is being closed. May be vetoed using a {@link VetoException}
   */
  @ConfigOperation
  @Order(15)
  protected ContributionCommand execClosing() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a UI is attached. The desktop must not be necessarily be open.
   */
  @ConfigOperation
  @Order(20)
  protected ContributionCommand execGuiAttached() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a UI is detached. The desktop must not be necessarily be open.
   */
  @ConfigOperation
  @Order(25)
  protected ContributionCommand execGuiDetached() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called whenever a new outline is activated on the desktop.
   */
  @ConfigOperation
  @Order(30)
  protected ContributionCommand execOutlineChanged(IOutline oldOutline, IOutline newOutline) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after an other page was selected.
   * 
   * @param oldForm
   *          is the search form of the old (not selected anymore) page or null
   * @param newForm
   *          is the search form of the new (selected) page or null
   */
  @Order(40)
  @ConfigOperation
  protected ContributionCommand execPageSearchFormChanged(IForm oldForm, IForm newForm) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after an other page was selected.
   * 
   * @param oldForm
   *          is the detail form of the old (not selected anymore) page or null
   * @param newForm
   *          is the detail form of the new (selected) page or null
   */
  @Order(50)
  @ConfigOperation
  protected ContributionCommand execPageDetailFormChanged(IForm oldForm, IForm newForm) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after an other page was selected.
   * 
   * @param oldForm
   *          is the table of the old (not selected anymore) table page or null
   * @param newForm
   *          is the table of the new (selected) table page or null
   */
  @Order(60)
  @ConfigOperation
  protected ContributionCommand execPageDetailTableChanged(ITable oldTable, ITable newTable) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a page was loaded or reloaded.
   * <p>
   * Default minimizes page search form when data was found.
   * 
   * @param page
   */
  @Order(62)
  @ConfigOperation
  protected ContributionCommand execTablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  /**
   * Invoked when the tray popup is being built.
   * <p>
   * May use {@link #getMenu(Class)} to find an existing menu in the desktop by class type.
   * <p>
   * The (potential) menus added to the list will be post processed. {@link IMenu#prepareAction()} is called on each and
   * the checked if the menu is visible.
   */
  @Order(70)
  @ConfigOperation
  protected ContributionCommand execAddTrayMenus(List<IMenu> menus) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @ConfigProperty(ConfigProperty.OUTLINES)
  @Order(20)
  @ConfigPropertyValue("null")
  protected Class<? extends IOutline>[] getConfiguredOutlines() {
    return null;
  }

  private Class<? extends IAction>[] getConfiguredActions() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IAction.class);
  }

}
