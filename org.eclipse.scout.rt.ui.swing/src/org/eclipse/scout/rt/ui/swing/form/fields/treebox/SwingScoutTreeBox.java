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
package org.eclipse.scout.rt.ui.swing.form.fields.treebox;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.tree.SwingScoutTree;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTreeEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutTreeBox extends SwingScoutValueFieldComposite<ITreeBox<?>> implements ISwingScoutTreeBox {
  private SwingScoutTree m_treeComposite;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    m_treeComposite = new SwingScoutTree();
    m_treeComposite.createField(getScoutObject().getTree(), getSwingEnvironment());
    //adjust row height
    JTree tree = m_treeComposite.getSwingTree();
    int rowHeight = -1;
    if (UIManager.get("TreeBox.rowHeight") != null) {
      rowHeight = UIManager.getInt("TreeBox.rowHeight");
    }
    if (rowHeight <= 0 && UIManager.get("ListBox.rowHeight") != null) {
      rowHeight = UIManager.getInt("ListBox.rowHeight");
    }
    if (rowHeight > 0) {
      tree.setRowHeight(rowHeight);
    }
    //
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData());
    m_treeComposite.getSwingScrollPane().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, fieldData);
    container.add(m_treeComposite.getSwingScrollPane());
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
    setSwingField(m_treeComposite.getSwingTree());
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JScrollPane getSwingScrollPane() {
    return m_treeComposite.getSwingScrollPane();
  }

  @Override
  public JTreeEx getSwingTree() {
    return m_treeComposite.getSwingTree();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // no super call, don't disable table to further support selection and menus
    getSwingLabel().setEnabled(b);
    getSwingField().repaint();
    getSwingScrollPane().getViewport().setOpaque(b);
  }

}
