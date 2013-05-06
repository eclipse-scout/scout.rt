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
package org.eclipse.scout.rt.ui.swing.form.fields.tablefield;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.basic.table.ISwingScoutTable;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableHeaderEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

public class SwingScoutTableField extends SwingScoutFieldComposite<ITableField<?>> implements ISwingScoutTableField {

  private ISwingScoutTable m_tableComposite;
  private ISwingTableStatus m_swingTableStatus;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setName(getScoutObject().getClass().getName() + ".container");
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    m_swingTableStatus = createSwingTableStatus(container);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    // layout
    container.setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  protected ISwingTableStatus createSwingTableStatus(JComponent container) {
    if (getScoutObject().isTableStatusVisible()) {
      //
      return new SwingTableStatus(getSwingEnvironment(), container, getScoutObject());
    }
    return null;
  }

  @Override
  public JScrollPane getSwingScrollPane() {
    return m_tableComposite != null ? m_tableComposite.getSwingScrollPane() : null;
  }

  @Override
  public JTableEx getSwingTable() {
    return m_tableComposite != null ? m_tableComposite.getSwingTable() : null;
  }

  @Override
  public ISwingTableStatus getSwingTableStatus() {
    return m_swingTableStatus;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setTableFromScout();
    setTableStatusFromScout();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // no super call, don't disable table to further support selection and menus
    getSwingLabel().setEnabled(b);
    if (getSwingScrollPane() != null) {
      getSwingScrollPane().getViewport().setOpaque(b);
    }
    // bugzilla 364121 >>
    if (getSwingTable() != null) {
      getSwingTable().repaint();
      if (getSwingTable().getTableHeader() != null) {
        getSwingTable().getTableHeader().repaint();
      }
    }
    // << bugzilla 364121
  }

  protected void setTableStatusFromScout() {
    if (m_swingTableStatus != null) {
      IProcessingStatus dataStatus = getScoutObject().getTablePopulateStatus();
      IProcessingStatus selectionStatus = getScoutObject().getTableSelectionStatus();
      m_swingTableStatus.setStatus(dataStatus, selectionStatus);
    }
  }

  protected void setTableFromScout() {
    ITable oldTable = m_tableComposite != null ? m_tableComposite.getScoutObject() : null;
    ITable newTable = getScoutObject().getTable();
    if (oldTable != newTable) {
      JComponent container = getSwingContainer();
      if (m_tableComposite != null) {
        container.remove(m_tableComposite.getSwingScrollPane());
        setSwingField(null);
        m_tableComposite.disconnectFromScout();
        m_tableComposite = null;
      }
      if (newTable != null) {
        ISwingScoutTable newTableComposite = getSwingEnvironment().createTable(newTable);
        newTableComposite.createField(newTable, getSwingEnvironment());

        // TODO replace with AbstractSwingEnvironment.createFormField.
        IForm form = (getScoutObject() != null) ? getScoutObject().getForm() : null;
        JTableEx newSwingTable = newTableComposite.getSwingTable();
        if (newSwingTable != null && form != null && IForm.VIEW_ID_PAGE_TABLE.equals(form.getDisplayViewId())) {
          newSwingTable.setName("Synth.WideTable");
          if (newSwingTable.getTableHeader() instanceof JTableHeaderEx) {
            ((JTableHeaderEx) newSwingTable.getTableHeader()).updatePreferredHeight();
          }
        }

        newTableComposite.getSwingScrollPane().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
        // top level table in top-level form has no border
        if (getScoutObject().getForm().getOuterForm() == null) {
          if (getScoutObject().getParentField() instanceof IGroupBox) {
            IGroupBox g = (IGroupBox) getScoutObject().getParentField();
            if (g.isMainBox() && !g.isBorderVisible()) {
              JScrollPane scrollPane = newTableComposite.getSwingScrollPane();
              scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            }
          }
        }
        m_tableComposite = newTableComposite;
        container.add(newTableComposite.getSwingScrollPane());
        setSwingField(newTableComposite.getSwingTable());
        container.revalidate();
        container.repaint();
      }
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITableField.PROP_TABLE)) {
      setTableFromScout();
    }
    else if (name.equals(ITableField.PROP_TABLE_SELECTION_STATUS)) {
      setTableStatusFromScout();
    }
    else if (name.equals(ITableField.PROP_TABLE_POPULATE_STATUS)) {
      setTableStatusFromScout();
    }
  }
}
