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

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * A desktop extension can contribute to a core {@link IDesktop} and react on desktop state changes using the
 * {@code exec* } methods defined in {@link AbstractDesktopExtension}.
 * <ul>
 * <li>outlines (with pages)</li>
 * <li>actions (menu, keyStroke, toolButton, viewButton)</li>
 * </ul>
 */
@Bean
public interface IDesktopExtension {

  /**
   * Returns the core desktop that holds this desktop extension.
   *
   * @return the desktop that holds this extension
   */
  IDesktop getCoreDesktop();

  /**
   * Sets the core desktop that holds this desktop extension.
   *
   * @param desktop
   *          the desktop that holds this extension
   */
  void setCoreDesktop(IDesktop desktop);

  /**
   * Called while this desktop extension is initialized.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand initDelegate();

  /**
   * Called after the core desktop was opened and displayed on the GUI.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand desktopOpenedDelegate();

  /**
   * Called just after the core desktop receives the request to close the desktop, i.e. before the desktop gets into its
   * closing state. The desktop extension is allowed to veto the closing process by throwing a {@link VetoException}.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand desktopBeforeClosingDelegate();

  /**
   * Called before the core desktop is being closed.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand desktopClosingDelegate();

  /**
   * Called after a UI has been attached to the core desktop. The desktop must not necessarily be open.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand guiAttachedDelegate();

  /**
   * Called after a UI has been detached from the core desktop. The desktop must not necessarily be open.
   *
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand guiDetachedDelegate();

  /**
   * Called whenever a new outline has been activated on the core desktop.
   *
   * @param oldOutline
   *          old outline that was active before
   * @param newOutline
   *          new outline that is active after the change
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand outlineChangedDelegate(IOutline oldOutline, IOutline newOutline);

  /**
   * Called right before a form is shown and therefore added to the desktop. This means this method is called before any
   * UI is informed about the new form. The form is provided in a holder. This allows it to prevent the form being added
   * to the desktop (set reference to {@code null}), do some general modifications needed to be done prior UI
   * instantiation, or even replace it with a different instance.
   *
   * @param formHolder
   *          contains the form that will be added to the core desktop
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand formAboutToShowDelegate(IHolder<IForm> formHolder);

  /**
   * Called whenever a new page has been activated (selected) on the core desktop.
   *
   * @param oldForm
   *          is the search form of the old (not selected anymore) page or {@code null}
   * @param newForm
   *          is the search form of the new (selected) page or {@code null}
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand pageSearchFormChangedDelegate(IForm oldForm, IForm newForm);

  /**
   * Called whenever a new page has been activated (selected) on the core desktop.
   *
   * @param oldForm
   *          is the detail form of the old (not selected anymore) page or {@code null}
   * @param newForm
   *          is the detail form of the new (selected) page or {@code null}
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand pageDetailFormChangedDelegate(IForm oldForm, IForm newForm);

  /**
   * Called whenever a new page has been activated (selected) on the core desktop.
   *
   * @param oldTable
   *          is the table of the old (not selected anymore) table page or {@code null}
   * @param newTable
   *          is the table of the new (selected) table page or {@code null}
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand pageDetailTableChangedDelegate(ITable oldTable, ITable newTable);

  /**
   * Called after a table page was loaded or reloaded.
   *
   * @param tablePage
   *          the table page that has been (re)loaded
   * @return {@code ContributionCommand.Continue} if further extensions should be processed,
   *         {@code ContributionCommand.Stop} otherwise
   */
  ContributionCommand tablePageLoadedDelegate(IPageWithTable<?> tablePage);

  /**
   * Adds the outlines configured with this extension to the {@code outlines} collection. This is a live list of
   * contributed outlines. They are NOT yet initialized.
   * <p>
   * Use the {@link Order} annotation or {@link IOutline#setOrder(double)} to define the sort order of the contributed
   * outlines.
   *
   * @param outlines
   *          a live collection to which the contributed outlines are added
   */
  void contributeOutlines(OrderedCollection<IOutline> outlines);

  /**
   * Adds the actions configured with this extension to the {@code actions} collection. This is a live list of
   * contributed actions ({@link IMenu}, {@link IKeyStroke}, {@link IViewButton}). They are NOT yet initialized.
   * <p>
   * Use the {@link Order} annotation or {@link IAction#setOrder(double)} to define the sort order of the contributed
   * actions.
   *
   * @param actions
   *          a live list to which the contributed actions are added
   */
  void contributeActions(Collection<IAction> actions);

}
