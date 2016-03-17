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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * base implementation of {@link IDesktopExtension}
 */
public abstract class AbstractDesktopExtension implements IDesktopExtension {
  private IDesktop m_coreDesktop;

  public AbstractDesktopExtension() {
  }

  @Override
  public ContributionCommand initDelegate() {
    return execInit();
  }

  @Override
  public ContributionCommand desktopOpenedDelegate() {
    return execOpened();
  }

  @Override
  public ContributionCommand desktopBeforeClosingDelegate() {
    return execBeforeClosing();
  }

  @Override
  public ContributionCommand desktopClosingDelegate() {
    return execClosing();
  }

  @Override
  public ContributionCommand guiAttachedDelegate() {
    return execGuiAttached();
  }

  @Override
  public ContributionCommand guiDetachedDelegate() {
    return execGuiDetached();
  }

  @Override
  public ContributionCommand outlineChangedDelegate(IOutline oldOutline, IOutline newOutline) {
    return execOutlineChanged(oldOutline, newOutline);
  }

  @Override
  public ContributionCommand formAboutToShowDelegate(IHolder<IForm> formHolder) {
    return execFormAboutToShow(formHolder);
  }

  @Override
  public ContributionCommand pageSearchFormChangedDelegate(IForm oldForm, IForm newForm) {
    return execPageSearchFormChanged(oldForm, newForm);
  }

  @Override
  public ContributionCommand pageDetailFormChangedDelegate(IForm oldForm, IForm newForm) {
    return execPageDetailFormChanged(oldForm, newForm);
  }

  @Override
  public ContributionCommand pageDetailTableChangedDelegate(ITable oldTable, ITable newTable) {
    return execPageDetailTableChanged(oldTable, newTable);
  }

  @Override
  public ContributionCommand tablePageLoadedDelegate(IPageWithTable<?> tablePage) {
    return execTablePageLoaded(tablePage);
  }

  @Override
  public void contributeOutlines(OrderedCollection<IOutline> outlines) {
    List<Class<? extends IOutline>> contributedOutlines = getConfiguredOutlines();
    if (contributedOutlines == null) {
      return;
    }
    for (Class<? extends IOutline> element : contributedOutlines) {
      try {
        IOutline o = element.newInstance();
        outlines.addOrdered(o);
      }
      catch (Exception t) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + element.getName() + "'.", t));
      }
    }
  }

  @Override
  public void contributeActions(Collection<IAction> actions) {
    for (Class<? extends IAction> actionClazz : getConfiguredActions()) {
      actions.add(ConfigurationUtility.newInnerInstance(this, actionClazz));
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
   */
  @ConfigOperation
  @Order(10)
  protected ContributionCommand execInit() {
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
   */
  @ConfigOperation
  @Order(12)
  protected ContributionCommand execOpened() {
    return ContributionCommand.Continue;
  }

  /**
   * Called just after the core desktop receives the request to close the desktop. This allows the desktop extension to
   * execute custom code before the desktop gets into its closing state. By throwing an explicit {@link VetoException}
   * the closing process will be stopped.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  @ConfigOperation
  @Order(14)
  protected ContributionCommand execBeforeClosing() {
    return ContributionCommand.Continue;
  }

  /**
   * Called before the core desktop is being closed.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  @ConfigOperation
  @Order(15)
  protected ContributionCommand execClosing() {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a UI has been attached to the core desktop. The desktop must not necessarily be open.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  @ConfigOperation
  @Order(20)
  protected ContributionCommand execGuiAttached() {
    return ContributionCommand.Continue;
  }

  /**
   * Called after a UI has been detached from the core desktop. The desktop must not necessarily be open.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  @ConfigOperation
  @Order(25)
  protected ContributionCommand execGuiDetached() {
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
   */
  @ConfigOperation
  @Order(30)
  protected ContributionCommand execOutlineChanged(IOutline oldOutline, IOutline newOutline) {
    return ContributionCommand.Continue;
  }

  /**
   * Called right before a form is shown and therefore added to the desktop. This means this method is called before any
   * UI is informed about the new form. The form is provided in a holder. This allows it to prevent the form being added
   * to the desktop (set reference to {@code null}), do some general modifications needed to be done prior UI
   * instantiation, or even replace it with a different instance.
   * <p>
   * Subclasses can override this method. The default simply returns {@link ContributionCommand.Continue}.
   *
   * @param formHolder
   *          contains the form that will be added to the core desktop
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  protected ContributionCommand execFormAboutToShow(IHolder<IForm> formHolder) {
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
   */
  @Order(40)
  @ConfigOperation
  protected ContributionCommand execPageSearchFormChanged(IForm oldForm, IForm newForm) {
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
   */
  @Order(50)
  @ConfigOperation
  protected ContributionCommand execPageDetailFormChanged(IForm oldForm, IForm newForm) {
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
   */
  @Order(60)
  @ConfigOperation
  protected ContributionCommand execPageDetailTableChanged(ITable oldTable, ITable newTable) {
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
   */
  @Order(62)
  @ConfigOperation
  protected ContributionCommand execTablePageLoaded(IPageWithTable<?> tablePage) {
    return ContributionCommand.Continue;
  }

  /**
   * Configures the outlines contributed by this desktop extension. These outlines are automatically added to the core
   * desktop which holds this extension.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return an array of outline type tokens
   * @see IOutline
   */
  @ConfigProperty(ConfigProperty.OUTLINES)
  @Order(20)
  protected List<Class<? extends IOutline>> getConfiguredOutlines() {
    return null;
  }

  private List<Class<? extends IAction>> getConfiguredActions() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IAction>> fca = ConfigurationUtility.filterClasses(dca, IAction.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

}
