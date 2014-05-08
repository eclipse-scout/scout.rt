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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

public abstract class AbstractMenu extends AbstractActionNode<IMenu> implements IMenu {

  private boolean m_singleSelectionAction;
  private boolean m_multiSelectionAction;
  private boolean m_emptySpaceAction;
  private IPropertyObserver m_owner;

  public AbstractMenu() {
    super();
  }

  public AbstractMenu(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * @return
   * @deprecated use {@link AbstractTableMenu} as superclass and
   *             {@link AbstractTableMenu#getConfiguredSingleSelectionAction()} instead.
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredSingleSelectionAction() {
    return true;
  }

  /**
   * @return
   * @deprecated use {@link AbstractTableMenu} as superclass and
   *             {@link AbstractTableMenu#getConfiguredMultiSelectionAction()} instead.
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredMultiSelectionAction() {
    return false;
  }

  /**
   * @return
   * @deprecated use {@link AbstractTableMenu} as superclass and
   *             {@link AbstractTableMenu#getConfiguredEmptySpaceAction()} instead.
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredEmptySpaceAction() {
    return false;
  }

  @Override
  public void handleOwnerValueChanged(Object newValue) throws ProcessingException {
    execOwnerValueChanged(newValue);
  }

  /**
   * AFTER a new valid master value was stored, this method is called
   */
  @ConfigOperation
  @Order(50.0)
  protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
    if (hasChildActions()) {
      return;
    }
    // lagacy support
    boolean visible = false;
    if (newOwnerValue instanceof Collection) {

      Collection collectionValue = (Collection) newOwnerValue;
      if (isEmptySpaceAction()) {
        visible = collectionValue.isEmpty();
      }
      else {
        Collection<ITableRow> rows = convertToTableRows(collectionValue);
        boolean allEnabled = true;
        if (rows != null) {
          for (ITableRow r : rows) {
            if (!r.isEnabled()) {
              allEnabled = false;
              break;
            }
          }
          if (isSingleSelectionAction() && allEnabled) {
            visible = collectionValue.size() == 1;
          }
          if (!visible && isMultiSelectionAction() && allEnabled) {
            visible = collectionValue.size() > 1;
          }
        }
      }
    }
    else {
      if (isSingleSelectionAction()) {
        visible = newOwnerValue != null;
      }
      if (isMultiSelectionAction()) {
        visible = newOwnerValue != null;
      }
      if (isEmptySpaceAction()) {
        visible = newOwnerValue == null;
      }
    }
    setVisible(visible);
  }

  /**
   * converts a untyped collection into a type collection of table rows.
   * 
   * @param input
   * @return null if the input is null or not all elements of the input are {@link ITableRow}s.
   */
  protected Collection<ITableRow> convertToTableRows(Collection<?> input) {
    if (input == null) {
      return null;
    }
    List<ITableRow> rows = new ArrayList<ITableRow>(input.size());
    for (Object o : input) {
      if (o instanceof ITableRow) {
        rows.add((ITableRow) o);
      }
    }
    if (rows.size() == input.size()) {
      return rows;
    }
    return null;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSingleSelectionAction(getConfiguredSingleSelectionAction());
    setMultiSelectionAction(getConfiguredMultiSelectionAction());
    setEmptySpaceAction(getConfiguredEmptySpaceAction());
    if (isSingleSelectionAction() || isMultiSelectionAction() || isEmptySpaceAction()) {
      // ok
    }
    else {
      // legacy case of implicit new menu
      setEmptySpaceAction(true);
    }
  }

  @Override
  public void setOwnerInternal(IPropertyObserver owner) {
    m_owner = owner;
  }

  @Override
  public IPropertyObserver getOwner() {
    return m_owner;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isSingleSelectionAction() {
    return m_singleSelectionAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setSingleSelectionAction(boolean b) {
    m_singleSelectionAction = b;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isMultiSelectionAction() {
    return m_multiSelectionAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setMultiSelectionAction(boolean b) {
    m_multiSelectionAction = b;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isEmptySpaceAction() {
    return m_emptySpaceAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setEmptySpaceAction(boolean b) {
    m_emptySpaceAction = b;
  }

}
