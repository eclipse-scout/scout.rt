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
package org.eclipse.scout.rt.ui.swing.form.fields.treefield;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.basic.tree.ISwingScoutTree;
import org.eclipse.scout.rt.ui.swing.basic.tree.SwingScoutTree;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTreeEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

public class SwingScoutTreeField extends SwingScoutFieldComposite<ITreeField> implements ISwingScoutTreeField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutTreeField.class);

  private ISwingScoutTree m_treeComposite;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    //
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    //
    setSwingLabel(label);
    setSwingContainer(container);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  public JScrollPane getSwingScrollPane() {
    return m_treeComposite != null ? m_treeComposite.getSwingScrollPane() : null;
  }

  public JTreeEx getSwingTree() {
    return m_treeComposite != null ? m_treeComposite.getSwingTree() : null;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setTreeFromScout();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // no super call, don't disable tree to further support selection and menus
    getSwingLabel().setEnabled(b);
    if (getSwingScrollPane() != null) {
      getSwingScrollPane().getViewport().setOpaque(b);
    }
  }

  protected void setTreeFromScout() {
    ITree oldTree = m_treeComposite != null ? m_treeComposite.getScoutObject() : null;
    ITree newTree = getScoutObject().getTree();
    if (oldTree != newTree) {
      JComponent container = getSwingContainer();
      if (m_treeComposite != null) {
        container.remove(m_treeComposite.getSwingScrollPane());
        setSwingField(null);
        m_treeComposite.disconnectFromScout();
        m_treeComposite = null;
      }
      if (newTree != null) {
        ISwingScoutTree newTreeComposite = new SwingScoutTree();
        newTreeComposite.createField(newTree, getSwingEnvironment());
        newTreeComposite.getSwingScrollPane().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
        // top level tree in form has no border
        JScrollPane scrollPane = newTreeComposite.getSwingScrollPane();
        if (getScoutObject().getParentField() instanceof IGroupBox) {
          IGroupBox g = (IGroupBox) getScoutObject().getParentField();
          if (g.isMainBox() && !g.isBorderVisible()) {
            scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
          }
        }
        m_treeComposite = newTreeComposite;
        container.add(newTreeComposite.getSwingScrollPane());
        setSwingField(newTreeComposite.getSwingTree());
        container.revalidate();
        container.repaint();
      }
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITreeField.PROP_TREE)) {
      setTreeFromScout();
    }
  }

}
