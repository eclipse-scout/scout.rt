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
package org.eclipse.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @since 5.1.0
 */
@ClassId("8fa1676b-042d-4d56-b4ba-af620eeee4cb")
public abstract class AbstractTableControl extends AbstractAction implements ITableControl {
  private ITable m_table;

  public AbstractTableControl() {
    this(true);
  }

  public AbstractTableControl(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected boolean getConfiguredToggleAction() {
    return true;
  }

  public void setTable(ITable table) {
    m_table = table;
  }

  @Override
  public ITable getTable() {
    return m_table;
  }

  @Override
  protected void execSelectionChanged(boolean selected) {
    if (!selected) {
      return;
    }
    // Deselect other controls
    for (ITableControl control : m_table.getTableControls()) {
      if (control != this && control.isSelected()) {
        control.setSelected(false);
      }
    }
  }

}
