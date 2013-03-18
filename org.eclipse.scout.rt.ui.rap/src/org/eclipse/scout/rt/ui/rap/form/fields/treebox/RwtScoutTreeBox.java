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
package org.eclipse.scout.rt.ui.rap.form.fields.treebox;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.tree.IRwtScoutTree;
import org.eclipse.scout.rt.ui.rap.basic.tree.RwtScoutTree;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.0
 */
public class RwtScoutTreeBox extends RwtScoutValueFieldComposite<ITreeBox<?>> implements IRwtScoutTreeBox {

  private IRwtScoutTree m_treeComposite;
  private Composite m_treeContainer;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    // XXX create tree by using extension point (formField Extension),
    // m_treeComposite = getEnvironment().createTree(container,
    // getScoutObject().getTree()); //FIXME AHO: please finish this pending task. I disabled this line meanwhile because it doens't works. regards MHA
    Composite treeContainer = new Composite(container, SWT.NONE);
    treeContainer.setLayout(new LogicalGridLayout(1, 0));
    treeContainer.setData(RWT.CUSTOM_VARIANT, RwtUtility.VARIANT_LISTBOX);
    m_treeContainer = treeContainer;
    m_treeComposite = new RwtScoutTree();
    m_treeComposite.createUiField(treeContainer, getScoutObject().getTree(), getUiEnvironment());
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
    setUiField(m_treeComposite.getUiField());
    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
    // XXX from imo: basically m_treeComposite.getUiContainer() should be used here, might be null and only then m_treeComposite.getUiField() can be used.
    treeContainer.setLayoutData(fieldData);
  }

  /**
   * complete override
   */
  @Override
  protected void setFieldEnabled(Control field, boolean b) {
    if (m_treeComposite instanceof RwtScoutTree) {
      ((RwtScoutTree) m_treeComposite).setEnabledFromScout(b);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    // Workaround, because ":disabled" state seems to be ignored by RAP
    if (m_treeContainer != null) {
      m_treeContainer.setData(RWT.CUSTOM_VARIANT, (b ? RwtUtility.VARIANT_LISTBOX : RwtUtility.VARIANT_LISTBOX_DISABLED));
    }
  }
}
