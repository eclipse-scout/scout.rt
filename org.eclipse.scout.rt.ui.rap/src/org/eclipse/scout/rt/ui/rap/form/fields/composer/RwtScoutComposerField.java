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
package org.eclipse.scout.rt.ui.rap.form.fields.composer;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.basic.tree.IRwtScoutTree;
import org.eclipse.scout.rt.ui.rap.basic.tree.RwtScoutTree;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutComposerField extends RwtScoutFieldComposite<IComposerField> implements IRwtScoutComposerField {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutComposerField.class);

  private IRwtScoutTree m_UiTreeComposite;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    // XXX create tree by using extension point (formField Extension),
    // m_treeComposite = getEnvironment().createTree(container, getScoutObject().getTree()); //FIXME AHO: please finish this pending task. I disabled this line meanwhile because it doens't works. regards MHA
    m_UiTreeComposite = new RwtScoutTree();
    Composite treeContainer = new Composite(container, SWT.NONE);
    treeContainer.setLayout(new LogicalGridLayout(1, 0));
    IForm form = (getScoutObject() != null) ? getScoutObject().getForm() : null;
    if (form != null && !IForm.VIEW_ID_PAGE_TABLE.equals(form.getDisplayViewId())) {
      treeContainer.setData(WidgetUtil.CUSTOM_VARIANT, RwtUtility.VARIANT_LISTBOX);
    }
    m_UiTreeComposite.createUiField(treeContainer, getScoutObject().getTree(), getUiEnvironment());
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    //
    setUiContainer(container);
    setUiLabel(label);
    setUiField(m_UiTreeComposite.getUiField());
    //layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
    //XXX from imo: basically m_treeComposite.getSwtContainer() should be used here, might be null and only then m_treeComposite.getSwtField() can be used.
    treeContainer.setLayoutData(fieldData);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    if (m_UiTreeComposite instanceof RwtScoutTree) {
      ((RwtScoutTree) m_UiTreeComposite).setEnabledFromScout(b);
    }
  }
}
