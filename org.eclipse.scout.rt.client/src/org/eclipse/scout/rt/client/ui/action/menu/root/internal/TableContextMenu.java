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
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractPropertyObserverContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * The invisible root menu node of any table. (internal usage only)
 */
public class TableContextMenu extends AbstractPropertyObserverContextMenu<ITable> implements ITableContextMenu {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableContextMenu.class);

  /**
   * @param owner
   */
  public TableContextMenu(ITable owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    getOwner().addTableListener(new P_OwnerTableListener());
    // set active filter
    setActiveFilter(ActionUtility.createMenuFilterForTableSelection(getOwner().getSelectedRows()));
    calculateLocalVisibility();
  }

  @Override
  protected void afterChildMenusAdd(List<? extends IMenu> newChildMenus) {
    super.afterChildMenusAdd(newChildMenus);
    handleOwnerEnabledChanged();
  }

  @Override
  protected void afterChildMenusRemove(List<? extends IMenu> childMenusToRemove) {
    super.afterChildMenusRemove(childMenusToRemove);
    handleOwnerEnabledChanged();
  }

  /**
  *
  */
  protected void handleOwnerEnabledChanged() {
    if (getOwner() != null) {
      final boolean enabled = getOwner().isEnabled();
      acceptVisitor(new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            if (!menu.hasChildActions() && menu.isInheritAccessibility()) {
              menu.setEnabled(enabled);
            }
          }
          return CONTINUE;
        }
      });
    }
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    if (getOwner() != null) {
      final List<ITableRow> ownerValue = getOwner().getSelectedRows();
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
      setActiveFilter(ActionUtility.createMenuFilterForTableSelection(ownerValue));
      calculateLocalVisibility();
    }
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    if (ITable.PROP_ENABLED.equals(evt.getPropertyName())) {
      handleOwnerEnabledChanged();
    }
  }

  private class P_OwnerTableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      if (e.getType() == TableEvent.TYPE_ROWS_SELECTED) {
        handleOwnerValueChanged();
      }
    }
  }
}
