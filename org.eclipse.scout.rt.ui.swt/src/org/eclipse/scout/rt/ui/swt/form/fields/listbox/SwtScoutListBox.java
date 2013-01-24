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
package org.eclipse.scout.rt.ui.swt.form.fields.listbox;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.basic.table.SwtScoutTable;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.ext.table.TableEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>SwtScoutListBox</h3> ...
 * 
 * @since 1.0.0 12.04.2008
 */
public class SwtScoutListBox extends SwtScoutValueFieldComposite<IListBox<?>> implements ISwtScoutListBox {

  private SwtScoutTable m_tableComposite;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    m_tableComposite = new SwtScoutTable();
    m_tableComposite.createField(container, getScoutObject().getTable(), getEnvironment());
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    // filter box
    IFormField[] childFields = getScoutObject().getFields();
    if (childFields.length > 0) {
      ISwtScoutComposite filterComposite = getEnvironment().createFormField(container, childFields[0]);
      LogicalGridData filterData = LogicalGridDataBuilder.createField(childFields[0].getGridData());
      filterData.gridx = fieldData.gridx;
      filterData.gridy = fieldData.gridy + fieldData.gridh;
      filterData.gridw = fieldData.gridw;
      filterData.weightx = fieldData.weightx;
      filterComposite.getSwtContainer().setLayoutData(filterData);
    }
    //
    setSwtContainer(container);
    setSwtLabel(label);
    TableEx tableField = m_tableComposite.getSwtField();
    tableField.setLayoutData(fieldData);
    setSwtField(tableField);

    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  /**
   * complete override
   */
  @Override
  protected void setFieldEnabled(Control swtField, boolean b) {
    m_tableComposite.setEnabledFromScout(b);
  }

}
