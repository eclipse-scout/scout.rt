/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * The invisible root menu node of any table. (internal usage only)
 */
@ClassId("97f17065-0142-4362-9dd4-a34148e20bb3")
public class TableContextMenu extends AbstractContextMenu<ITable> implements ITableContextMenu {
  private List<? extends ITableRow> m_currentSelection;

  public TableContextMenu(ITable owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    ITable container = getContainer();
    container.addTableListener(this::tableChanged, TableEvent.TYPE_ROWS_SELECTED, TableEvent.TYPE_ROWS_UPDATED);
    // set active filter
    setCurrentMenuTypes(getMenuTypesForSelection(container.getSelectedRows()));
    calculateLocalVisibility();
  }

  protected void tableChanged(TableEvent e) {
    switch (e.getType()) {
      case TableEvent.TYPE_ROWS_SELECTED:
        handleOwnerValueChanged();
        break;
      case TableEvent.TYPE_ROWS_UPDATED:
        if (CollectionUtility.containsAny(e.getRows(), m_currentSelection)) {
          handleOwnerValueChanged();
        }
        break;
    }
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  @Override
  protected boolean isOwnerPropertyChangedListenerRequired() {
    return true;
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    super.handleOwnerPropertyChanged(evt);
    if (IWidget.PROP_ENABLED.equals(evt.getPropertyName())) {
      calculateEnabledState();
    }
  }

  protected void handleOwnerValueChanged() {
    m_currentSelection = null;
    ITable container = getContainer();
    if (container != null) {
      final List<ITableRow> ownerValue = container.getSelectedRows();
      m_currentSelection = CollectionUtility.arrayList(ownerValue);
      setCurrentMenuTypes(getMenuTypesForSelection(ownerValue));
      visit(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()), IMenu.class);
      calculateLocalVisibility();
      calculateEnabledState();
    }
  }

  protected boolean isSelectionEnabled() {
    return getContainer()
        .getSelectedRows().stream()
        .allMatch(ITableRow::isEnabled);
  }

  /**
   * called on selection change (selected rows) or when the table enabled state changes
   */
  protected void calculateEnabledState() {
    ActionUtility.updateContextMenuEnabledState(this, this::isSelectionEnabled, TableMenuType.MultiSelection, TableMenuType.SingleSelection);
  }

  protected Set<TableMenuType> getMenuTypesForSelection(List<? extends ITableRow> selection) {
    if (CollectionUtility.isEmpty(selection)) {
      return CollectionUtility.hashSet(TableMenuType.EmptySpace);
    }
    if (CollectionUtility.size(selection) == 1) {
      return CollectionUtility.hashSet(TableMenuType.SingleSelection);
    }
    return CollectionUtility.hashSet(TableMenuType.MultiSelection);
  }
}
