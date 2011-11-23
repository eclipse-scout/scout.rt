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
package org.eclipse.scout.rt.ui.swt.form.fields.treefield;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.tree.SwtScoutTree;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>SwtScoutTreeBox</h3> ...
 * 
 * @since 1.0.0 15.04.2008
 */
public class SwtScoutTreeField extends SwtScoutFieldComposite<ITreeField> implements ISwtScoutTreeField {

  private SwtScoutTree m_treeComposite;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment());

    setSwtContainer(container);
    setSwtLabel(label);
    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  protected void attachScout() {
    setTreeFromScout(getScoutObject().getTree());
    super.attachScout();
  }

  protected void setTreeFromScout(ITree tree) {
    if (m_treeComposite != null) {
      m_treeComposite.dispose();
      m_treeComposite = null;
    }
    if (tree != null) {
      m_treeComposite = new SwtScoutTree();
      m_treeComposite.createField(getSwtContainer(), getScoutObject().getTree(), getEnvironment());
      m_treeComposite.getSwtField().setLayoutData(LogicalGridDataBuilder.createField(getScoutObject().getGridData()));
      setSwtField(m_treeComposite.getSwtField());
    }
    getSwtContainer().layout(true, true);
  }

  /**
   * complete override
   */
  @Override
  protected void setFieldEnabled(Control swtField, boolean b) {
    if (hasSwtTree()) {
      m_treeComposite.setEnabledFromScout(b);
    }
  }

  protected boolean hasSwtTree() {
    return m_treeComposite != null && m_treeComposite.isInitialized();
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITreeField.PROP_TREE)) {
      setTreeFromScout((ITree) newValue);
    }
  }

}
