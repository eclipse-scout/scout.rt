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
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractPropertyObserverContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IActivityMapContextMenu;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * The invisible root menu node of any activity map. (internal usage only)
 */
public class ActivityMapContextMenu extends AbstractPropertyObserverContextMenu<IActivityMap<?, ?>> implements IActivityMapContextMenu {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ActivityMapContextMenu.class);

  /**
   * @param owner
   */
  public ActivityMapContextMenu(IActivityMap<?, ?> owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // set active filter
    setCurrentMenuTypes(MenuUtility.getMenuTypesForActivityMapSelection(getOwner().getSelectedActivityCell()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    if (getOwner() != null) {
      final ActivityCell<?, ?> ownerValue = getOwner().getSelectedActivityCell();
      acceptVisitor(new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            try {
              menu.handleOwnerValueChanged(ownerValue);
            }
            catch (ProcessingException ex) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
            }
          }
          return CONTINUE;
        }
      });
      // set active filter
      setCurrentMenuTypes(MenuUtility.getMenuTypesForActivityMapSelection(ownerValue));

      calculateLocalVisibility();
    }
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    if (IActivityMap.PROP_SELECTED_ACTIVITY_CELL.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    else if (IActivityMap.PROP_SELECTED_RESOURCE_IDS.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }

  }

}
