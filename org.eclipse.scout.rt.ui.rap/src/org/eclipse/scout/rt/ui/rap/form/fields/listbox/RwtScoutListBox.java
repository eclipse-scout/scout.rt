/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.listbox;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.table.IRwtScoutTableForPatch;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.services.common.patchedclass.IPatchedClassService;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.0
 */
public class RwtScoutListBox extends RwtScoutValueFieldComposite<IListBox<?>> implements IRwtScoutListBox {

  private IRwtScoutTableForPatch m_tableComposite;
  private Composite m_tableContainer;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    Composite tableContainer = new Composite(container, SWT.NONE);
    tableContainer.setLayout(new LogicalGridLayout(1, 0));
    tableContainer.setData(RWT.CUSTOM_VARIANT, RwtUtility.VARIANT_LISTBOX);
    m_tableContainer = tableContainer;
    m_tableComposite = SERVICES.getService(IPatchedClassService.class).createRwtScoutTable(RwtUtility.VARIANT_LISTBOX);
    m_tableComposite.createUiField(tableContainer, getScoutObject().getTable(), getUiEnvironment());
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    // filter box
    IFormField[] childFields = getScoutObject().getFields();
    if (childFields.length > 0) {
      IRwtScoutComposite filterComposite = getUiEnvironment().createFormField(container, childFields[0]);
      LogicalGridData filterData = LogicalGridDataBuilder.createField(childFields[0].getGridData());
      filterData.gridx = fieldData.gridx;
      filterData.gridy = fieldData.gridy + fieldData.gridh;
      filterData.gridw = fieldData.gridw;
      filterData.weightx = fieldData.weightx;
      filterComposite.getUiContainer().setLayoutData(filterData);
    }
    //
    setUiContainer(container);
    setUiLabel(label);
    TableEx tableField = m_tableComposite.getUiField();
    tableContainer.setLayoutData(fieldData);
    setUiField(tableField);

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  /**
   * complete override
   */
  @Override
  protected void setFieldEnabled(Control uiField, boolean b) {
    m_tableComposite.setEnabledFromScout(b);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    // Workaround, because ":disabled" state seems to be ignored by RAP
    if (m_tableContainer != null) {
      m_tableContainer.setData(RWT.CUSTOM_VARIANT, (b ? RwtUtility.VARIANT_LISTBOX : RwtUtility.VARIANT_LISTBOX_DISABLED));
    }
  }
}
