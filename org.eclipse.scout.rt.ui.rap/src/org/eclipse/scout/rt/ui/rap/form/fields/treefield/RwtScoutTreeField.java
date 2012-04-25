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
package org.eclipse.scout.rt.ui.rap.form.fields.treefield;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.basic.tree.IRwtScoutTree;
import org.eclipse.scout.rt.ui.rap.basic.tree.RwtScoutTree;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutActionBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>RwtScoutTreeBox</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutTreeField extends RwtScoutFieldComposite<ITreeField> implements IRwtScoutTreeField {

  private IRwtScoutTree m_treeComposite;
  private Composite m_treeContainer;
  private IRwtScoutActionBar m_actionBar;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    setUiContainer(container);
    setUiLabel(label);
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

    if (m_actionBar != null) {
      m_actionBar.dispose();
    }

    if (tree != null) {
      m_treeComposite = createRwtScoutTree();

      IForm form = getScoutObject() == null ? null : getScoutObject().getForm();
      m_treeContainer = null;
      if (form == null ||
          form instanceof ISmartFieldProposalForm ||
          IForm.VIEW_ID_OUTLINE.equals(form.getDisplayViewId())) {
        m_treeComposite.createUiField(getUiContainer(), getScoutObject().getTree(), getUiEnvironment());
        m_treeComposite.getUiField().setLayoutData(LogicalGridDataBuilder.createField(getScoutObject().getGridData()));
      }
      //XXX somehow the tableContainer does absorb the mouse-clicks when used in the outline
      else {
        Composite treeContainer = new Composite(getUiContainer(), SWT.NONE);
        treeContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TREE_CONTAINER);
        treeContainer.setLayout(new LogicalGridLayout(1, 0));
        m_treeComposite.createUiField(treeContainer, getScoutObject().getTree(), getUiEnvironment());
        treeContainer.setLayoutData(LogicalGridDataBuilder.createField(getScoutObject().getGridData()));
        m_treeContainer = treeContainer;
      }

      m_actionBar = createRwtScoutActionBar();

      setUiField(m_treeComposite.getUiField());
    }
    getUiContainer().layout(true, true);
  }

  /**
   * @since 3.8.0
   */
  protected IRwtScoutTree createRwtScoutTree() {
    if (getScoutObject().getForm() instanceof ISmartFieldProposalForm) {
      return new RwtScoutTree(RwtUtility.VARIANT_PROPOSAL_FORM);
    }
    else {
      return new RwtScoutTree();
    }
  }

  /**
   * As default there is no action bar. <br/>
   * Subclasses can override this method to create one.
   */
  protected IRwtScoutActionBar createRwtScoutActionBar() {
    return null;
  }

  /**
   * complete override
   */
  @Override
  protected void setFieldEnabled(Control field, boolean b) {
    if (hasUiTree()) {
      m_treeComposite.setEnabledFromScout(b);
    }
  }

  protected boolean hasUiTree() {
    return m_treeComposite != null && m_treeComposite.isCreated();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    // Workaround, because ":disabled" state seems to be ignored by RAP
    if (m_treeContainer != null) {
      m_treeContainer.setData(WidgetUtil.CUSTOM_VARIANT, (b ? VARIANT_TREE_CONTAINER : VARIANT_TREE_CONTAINER_DISABLED));
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITreeField.PROP_TREE)) {
      setTreeFromScout((ITree) newValue);
    }
  }
}
