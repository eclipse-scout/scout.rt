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
  public void setChildActions(List<? extends IMenu> newList) {
    updateChildActions(newList);
  }

  protected void updateChildActions(List<? extends IMenu> newList) {
    updatePropertyListeners(newList);
    super.setChildActions(newList);
    calculateVisibility();
  }

  private void updatePropertyListeners(List<? extends IMenu> newList) {
    // remove old
    removeScoutMenuVisibilityListenerRec(getChildActions());
    // add new
    if (newList != null) {
      addScoutMenuVisibilityListenerRec(newList);
    }
  }

  protected void addScoutMenuVisibilityListenerRec(List<? extends IMenu> menus) {
    for (IMenu m : menus) {
      m.addPropertyChangeListener(IMenu.PROP_VISIBLE, m_menuVisibilityListener);
      addScoutMenuVisibilityListenerRec(m.getChildActions());
    }
  }

  protected void removeScoutMenuVisibilityListenerRec(List<? extends IMenu> menus) {
    for (IMenu m : menus) {
      m.removePropertyChangeListener(IMenu.PROP_VISIBLE, m_menuVisibilityListener);
      removeScoutMenuVisibilityListenerRec(m.getChildActions());
    }
  }

  private void calculateVisibility() {
    List<IMenu> visibleChildMenus = ActionUtility.visibleNormalizedActions(getChildActions());
    setVisible(CollectionUtility.hasElements(visibleChildMenus));
  }

  private class P_VisibilityOfMenuItemChangedListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
        calculateVisibility();
      }
    }

  }
}
