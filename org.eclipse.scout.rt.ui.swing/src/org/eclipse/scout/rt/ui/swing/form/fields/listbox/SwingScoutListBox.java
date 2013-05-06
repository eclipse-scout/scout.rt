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
package org.eclipse.scout.rt.ui.swing.form.fields.listbox;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.table.ISwingScoutTable;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutListBox extends SwingScoutValueFieldComposite<IListBox<?>> implements ISwingScoutListBox {
  private ISwingScoutTable m_tableComposite;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    ITable scoutTable = getScoutObject().getTable();
    m_tableComposite = getSwingEnvironment().createTable(scoutTable);
    m_tableComposite.createField(scoutTable, getSwingEnvironment());
    //adjust table row height
    JTable table = m_tableComposite.getSwingTable();
    if (UIManager.get("ListBox.rowHeight") != null) {
      int rowHeight = UIManager.getInt("ListBox.rowHeight");
      if (rowHeight > 0) {
        table.setRowHeight(rowHeight);
      }
    }
    //
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData());
    m_tableComposite.getSwingScrollPane().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, fieldData);
    container.add(m_tableComposite.getSwingScrollPane());
    // filter box
    IFormField[] childFields = getScoutObject().getFields();
    if (childFields.length > 0) {
      ISwingScoutComposite filterComposite = getSwingEnvironment().createFormField(container, childFields[0]);
      LogicalGridData filterData = LogicalGridDataBuilder.createField(getSwingEnvironment(), childFields[0].getGridData());
      filterData.gridx = fieldData.gridx;
      filterData.gridy = fieldData.gridy + fieldData.gridh;
      filterData.gridw = fieldData.gridw;
      filterData.weightx = fieldData.weightx;
      filterComposite.getSwingContainer().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, filterData);
      container.add(filterComposite.getSwingContainer());
    }
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(m_tableComposite.getSwingTable());
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JScrollPane getSwingScrollPane() {
    return m_tableComposite.getSwingScrollPane();
  }

  @Override
  public JTableEx getSwingTable() {
    return m_tableComposite.getSwingTable();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // no super call, don't disable table to further support selection and menus
    getSwingLabel().setEnabled(b);
    getSwingField().repaint();
    getSwingScrollPane().getViewport().setOpaque(b);
  }

}
