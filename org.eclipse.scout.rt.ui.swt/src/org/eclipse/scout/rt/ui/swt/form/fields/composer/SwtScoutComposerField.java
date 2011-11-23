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
package org.eclipse.scout.rt.ui.swt.form.fields.composer;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.tree.ISwtScoutTree;
import org.eclipse.scout.rt.ui.swt.basic.tree.SwtScoutTree;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.swt.widgets.Composite;

public class SwtScoutComposerField extends SwtScoutFieldComposite<IComposerField> implements ISwtScoutComposerField {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutComposerField.class);

  private ISwtScoutTree m_treeComposite;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    // XXX create tree by using extension point (formField Extension),
    // m_treeComposite = getEnvironment().createTree(container, getScoutObject().getTree()); //FIXME AHO: please finish this pending task. I disabled this line meanwhile because it doens't works. regards MHA
    m_treeComposite = new SwtScoutTree();
    m_treeComposite.createField(container, getScoutObject().getTree(), getEnvironment());
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(m_treeComposite.getSwtField());
    //layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
    //XXX from imo: basically m_treeComposite.getSwtContainer() should be used here, might be null and only then m_treeComposite.getSwtField() can be used.
    m_treeComposite.getSwtField().setLayoutData(fieldData);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    if (m_treeComposite instanceof SwtScoutTree) {
      ((SwtScoutTree) m_treeComposite).setEnabledFromScout(b);
    }
  }
}
