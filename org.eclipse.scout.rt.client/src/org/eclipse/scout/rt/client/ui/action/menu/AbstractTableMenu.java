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

import java.util.List;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

/**
 *
 */
public class AbstractTableMenu extends AbstractMenu {

  @SuppressWarnings("deprecation")
  @Override
  protected boolean getConfiguredSingleSelectionAction() {
    return super.getConfiguredSingleSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean getConfiguredMultiSelectionAction() {
    return super.getConfiguredMultiSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean getConfiguredEmptySpaceAction() {
    return super.getConfiguredEmptySpaceAction();
  }

  @Override
  protected final void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
    execTableSelectionChanged(getOwner().getSelectedRows());
  }

  protected void execTableSelectionChanged(List<ITableRow> newSelection) {
    boolean visible = false;
    if (isEmptySpaceAction()) {
      visible = newSelection.isEmpty();
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
        if (isSingleSelectionAction()) {
          visible = newSelection.size() == 1;
        }
        if (!visible && isMultiSelectionAction()) {
          visible = newSelection.size() > 1;
        }
      }
    }
    setVisible(visible);
  }

  @Override
  public ITable getOwner() {
    return (ITable) super.getOwner();
  }

  @Override
  public void setOwnerInternal(IPropertyObserver owner) {
    if (owner instanceof ITable) {
      super.setOwnerInternal(owner);
    }
    else {
      throw new IllegalArgumentException("menu owner must be an instance of ITable.");
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isSingleSelectionAction() {
    return super.isSingleSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setSingleSelectionAction(boolean b) {
    super.setSingleSelectionAction(b);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isMultiSelectionAction() {
    return super.isMultiSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setMultiSelectionAction(boolean b) {
    super.setMultiSelectionAction(b);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isEmptySpaceAction() {
    return super.isEmptySpaceAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setEmptySpaceAction(boolean b) {
    super.setEmptySpaceAction(b);
  }
}
