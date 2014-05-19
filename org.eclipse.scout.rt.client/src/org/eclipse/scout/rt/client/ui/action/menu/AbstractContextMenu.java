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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;

/**
 *
 */
public abstract class AbstractContextMenu extends AbstractMenu implements IContextMenu {

  private PropertyChangeListener m_menuVisibilityListener = new P_VisibilityOfMenuItemChangedListener();

  public AbstractContextMenu() {
    super();
  }

  public AbstractContextMenu(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    calculateVisibility();
  }

  @Override
  protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
    // void here
  }

  @Override
  public void addChildActions(List<? extends IMenu> actionList) {
    super.addChildActions(actionList);
  }

  @Override
  protected void afterChildMenusAdd(List<? extends IMenu> newChildMenus) {
    super.afterChildMenusAdd(newChildMenus);
    addScoutMenuVisibilityListenerRec(newChildMenus);
    calculateVisibility();
  }

  @Override
  protected void afterChildMenusRemove(List<? extends IMenu> childMenusToRemove) {
    super.afterChildMenusRemove(childMenusToRemove);
    removeScoutMenuVisibilityListenerRec(childMenusToRemove);
    calculateVisibility();
  }

  /**
   * @param oldValue
   * @param newValue
   */
  protected void handleChildActionsChanged(List<IMenu> oldValue, List<IMenu> newValue) {
    removeScoutMenuVisibilityListenerRec(oldValue);
    addScoutMenuVisibilityListenerRec(newValue);
  }

  protected void addScoutMenuVisibilityListenerRec(List<? extends IMenu> menus) {
    if (menus != null) {
      for (IMenu m : menus) {
        m.addPropertyChangeListener(IMenu.PROP_CHILD_ACTIONS, m_menuVisibilityListener);
        m.addPropertyChangeListener(IMenu.PROP_VISIBLE, m_menuVisibilityListener);
        m.addPropertyChangeListener(IMenu.PROP_AVAILABLE, m_menuVisibilityListener);
        addScoutMenuVisibilityListenerRec(m.getChildActions());
      }
    }
  }

  protected void removeScoutMenuVisibilityListenerRec(List<? extends IMenu> menus) {
    if (menus != null) {
      for (IMenu m : menus) {
        m.removePropertyChangeListener(IMenu.PROP_CHILD_ACTIONS, m_menuVisibilityListener);
        m.removePropertyChangeListener(IMenu.PROP_VISIBLE, m_menuVisibilityListener);
        m.removePropertyChangeListener(IMenu.PROP_AVAILABLE, m_menuVisibilityListener);
        removeScoutMenuVisibilityListenerRec(m.getChildActions());
      }
    }
  }

  private void calculateVisibility() {
    List<IMenu> visibleChildMenus = ActionUtility.visibleNormalizedActions(getChildActions(), ActionUtility.createMenuFilterVisibleAvailable());
    setVisible(CollectionUtility.hasElements(visibleChildMenus));
  }

  private class P_VisibilityOfMenuItemChangedListener implements PropertyChangeListener {
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      if (IMenu.PROP_CHILD_ACTIONS.equals(evt.getPropertyName())) {
        handleChildActionsChanged((List<IMenu>) evt.getOldValue(), (List<IMenu>) evt.getNewValue());
      }
      calculateVisibility();
    }
  }

}
