/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.action;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironmentListener;
import org.eclipse.scout.rt.ui.swt.SwtEnvironmentEvent;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * <h3>AbstractSwtMenuContributionItem</h3>
 * <p>
 * Builds a swt menu based on the configuration in the desktop.
 * </p>
 * It is a {@link CompoundContributionItem} which can be inserted in any menu as a dynamic contribution.<br/>
 * It holds a set of {@link IMenuManager}s and {@link ActionContributionItem}s.
 * 
 * @author Claudio Guglielmo
 * @since 22.06.2010
 */
public abstract class AbstractSwtMenuContributionItem extends CompoundContributionItem {

  private List<IContributionItem> m_currentItems;
  private ISwtEnvironmentListener m_swtEnvironmentListener;

  public AbstractSwtMenuContributionItem() {
    m_swtEnvironmentListener = new SwtEnvironmentListener();
    getSwtEnvironment().addEnvironmentListener(m_swtEnvironmentListener);
  }

  @Override
  public void dispose() {
    if (m_swtEnvironmentListener != null) {
      getSwtEnvironment().removeEnvironmentListener(m_swtEnvironmentListener);
    }

    super.dispose();
  }

  protected abstract ISwtEnvironment getSwtEnvironment();

  @Override
  protected final IContributionItem[] getContributionItems() {
    IContributionItem[] items = new IContributionItem[0];

    if (getSwtEnvironment().isInitialized()) {
      items = collectContributionItems();
    }

    //Save the items in order to have access later
    m_currentItems = new LinkedList<IContributionItem>(Arrays.asList(items));

    return items;
  }

  protected IContributionItem[] collectContributionItems() {
    return SwtMenuUtility.getMenuContribution(getSwtEnvironment().getClientSession().getDesktop().getMenus(), getSwtEnvironment());
  }

  public List<IContributionItem> getCurrentContributionItems() {
    return m_currentItems;
  }

  protected class SwtEnvironmentListener implements ISwtEnvironmentListener {
    public void environmentChanged(SwtEnvironmentEvent e) {
      if (e.getType() == SwtEnvironmentEvent.STARTED) {

        // Since the contributionItems are not loaded until the user clicks on the menu
        // the keystrokes (accelerators) won't work. Therefore we have to force the load on startup.
        update();
      }
    }
  }

  @Override
  public void fill(Menu menu, int index) {
    super.fill(menu, index);

    //Update the childs so that the keystrokes are loaded even if the submenus have not been displayed yet.
    updateChilds();
  }

  @Override
  /**
   * Forces the parent (menuManager) to update.
   */
  public void update() {

    //The update leads to an execution of fill(menu, index) and getContributionItems().
    if (getParent() instanceof IMenuManager) {
      IMenuManager menuMgr = (IMenuManager) getParent();
      menuMgr.updateAll(true);
    }

  }

  /**
   * Forces every submenu to update so that the keystrokes will be set.
   */
  protected void updateChilds() {
    if (getCurrentContributionItems() != null) {
      for (IContributionItem contributionItem : getCurrentContributionItems()) {
        if (contributionItem instanceof IMenuManager) {
          IMenuManager childManager = (IMenuManager) contributionItem;
          childManager.updateAll(true);
        }
      }
    }
  }
}
