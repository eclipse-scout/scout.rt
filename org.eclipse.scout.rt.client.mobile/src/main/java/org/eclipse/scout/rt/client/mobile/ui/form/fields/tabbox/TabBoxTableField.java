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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractMobileTable;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.ClearTableSelectionFormCloseListener;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.TabBoxTableField.Table;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

/**
 * @since 3.9.0
 */
public class TabBoxTableField extends AbstractTableField<Table> {
  private ITabBox m_tabBox;

  public ITabBox getTabBox() {
    return m_tabBox;
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_tabBox = getConfiguredTabBox();
  }

  protected ITabBox getConfiguredTabBox() {
    return null;
  }

  @Override
  protected void execInitField() {
    if (m_tabBox == null) {
      return;
    }

    for (IGroupBox groupBox : m_tabBox.getGroupBoxes()) {
      groupBox.addPropertyChangeListener(new P_TabPropertyChangeListener());
    }

    rebuildTableRows();
  }

  public void rebuildTableRows() {
    if (m_tabBox == null) {
      return;
    }

    getTable().discardAllRows();

    for (IGroupBox groupBox : m_tabBox.getGroupBoxes()) {
      if (groupBox.isVisible()) {
        getTable().addRowByArray(new Object[]{groupBox, groupBox.getLabel()});
      }
    }

    getParentGroupBox().rebuildFieldGrid();
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Order(10)
  public class Table extends AbstractMobileTable {

    @Override
    protected void execRowsSelected(List<? extends ITableRow> rows) {
      if (getSelectedRow() == null) {
        return;
      }

      IGroupBox tab = getTabColumn().getValue(getSelectedRow());
      TabForm form = new TabForm(tab);
      form.getRootGroupBox().setFormInternal(getForm()); // set the original form to be the owner-form to have proper model context (Bugzilla 149246).
      form.setDisplayHint(getForm().getDisplayHint());
      form.setDisplayViewId(getForm().getDisplayViewId());
      form.setModal(IForm.DISPLAY_HINT_DIALOG == form.getDisplayHint());
      form.start();
      form.addFormListener(new ClearTableSelectionFormCloseListener(this));
    }

    @Override
    protected boolean execIsAutoCreateTableRowForm() {
      return false;
    }

    @Override
    protected boolean getConfiguredSortEnabled() {
      return false;
    }

    @Override
    protected boolean getConfiguredAutoResizeColumns() {
      return true;
    }

    @Override
    protected boolean getConfiguredMultiSelect() {
      return false;
    }

    public TabColumn getTabColumn() {
      return getColumnSet().getColumnByClass(TabColumn.class);
    }

    public LabelColumn getLabelColumn() {
      return getColumnSet().getColumnByClass(LabelColumn.class);
    }

    @Order(1)
    public class TabColumn extends AbstractColumn<IGroupBox> {

      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }

    }

    @Order(2)
    public class LabelColumn extends AbstractStringColumn {

    }
  }

  private class P_TabPropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      try {
        if (IGroupBox.PROP_VISIBLE.equals(evt.getPropertyName())) {
          rebuildTableRows();
        }
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }

    }
  }

}
