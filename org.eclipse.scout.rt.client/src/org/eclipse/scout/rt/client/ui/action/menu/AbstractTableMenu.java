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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

/**
 *
 */
public class AbstractTableMenu extends AbstractMenu implements ITableMenu {

  private boolean m_skipCalculateAvailability;

  public AbstractTableMenu() {
    super();
  }

  public AbstractTableMenu(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigProperty(ConfigProperty.TABLE_MENU_TYPE)
  @Order(140)
  protected EnumSet<TableMenuType> getConfiguredMenuType() {
    return EnumSet.<TableMenuType> of(TableMenuType.SingleSelection);
  }

  @Override
  protected final void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
    if (getOwner() != null) {
      List<ITableRow> newSelection = CollectionUtility.arrayList(getOwner().getSelectedRows());
      execTableSelectionChanged(newSelection);
    }
  }

  @Override
  protected void calculateAvailability(Object newOwnerValue) {
    if (m_skipCalculateAvailability) {
      return;
    }
    if (hasChildActions()) {
      setAvailableInternal(true);
      return;
    }
    List<ITableRow> newSelection = CollectionUtility.emptyArrayList();
    if (getOwner() != null) {
      newSelection = getOwner().getSelectedRows();
    }
    boolean available = false;
    if (newSelection.isEmpty()) {
      available = getMenuType().contains(TableMenuType.EmptySpace);
    }
    else {
      boolean allEnabled = true;
      for (ITableRow r : newSelection) {
        if (!r.isEnabled()) {
          allEnabled = false;
          break;
        }
      }
      if (allEnabled) {
        available |= (newSelection.size() == 1 && getMenuType().contains(TableMenuType.SingleSelection));
        available |= (newSelection.size() > 1 && getMenuType().contains(TableMenuType.MultiSelection));
      }
    }
    setAvailableInternal(available);

  }

  /**
   * selection of the owner table changed. Might be used to call {@link #setVisible(boolean)},
   * {@link #setEnabled(boolean)}, etc.
   * 
   * @param newSelection
   *          the current selection of the table
   */
  @ConfigOperation
  @Order(70.0)
  protected void execTableSelectionChanged(List<ITableRow> newSelection) {
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void initConfig() {
    // guard to ensure calculate availability is not called when only  legacy from super type is initialized
    try {
      m_skipCalculateAvailability = true;
      super.initConfig();
    }
    finally {
      m_skipCalculateAvailability = false;
    }
    if (!ConfigurationUtility.isMethodOverwrite(AbstractTableMenu.class, "getConfiguredMenuType", new Class[0], this.getClass())) {
      // legacy
      Set<TableMenuType> menuType = new HashSet<AbstractTableMenu.TableMenuType>();
      if (isSingleSelectionAction()) {
        menuType.add(TableMenuType.SingleSelection);
      }
      if (isMultiSelectionAction()) {
        menuType.add(TableMenuType.MultiSelection);
      }
      if (isEmptySpaceAction()) {
        menuType.add(TableMenuType.EmptySpace);
      }
      EnumSet<TableMenuType> menuTypeEnumSet = EnumSet.<TableMenuType> copyOf(menuType);
      setMenuType(menuTypeEnumSet);
    }
    else {
      setMenuType(getConfiguredMenuType());
    }
    calculateAvailability(null);
  }

  @Override
  public ITable getOwner() {
    return (ITable) super.getOwner();
  }

  @Override
  public void setOwnerInternal(IPropertyObserver owner) {
    if (owner == null || owner instanceof ITable) {
      super.setOwnerInternal(owner);
    }
    else {
      throw new IllegalArgumentException("menu owner must be an instance of ITable.");
    }
  }

  public void setMenuType(EnumSet<TableMenuType> menuType) {
    propertySupport.setProperty(PROP_MENU_TYPE, menuType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EnumSet<TableMenuType> getMenuType() {
    return (EnumSet<TableMenuType>) propertySupport.getProperty(PROP_MENU_TYPE);
  }

}
