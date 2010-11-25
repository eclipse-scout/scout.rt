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
package org.eclipse.scout.rt.ui.swing.form.fields.composer;

import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.basic.tree.SwingScoutTree;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTreeEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

public class SwingScoutComposerField extends SwingScoutFieldComposite<IComposerField> implements ISwingScoutComposerField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutComposerField.class);

  private SwingScoutTree m_treeComposite;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    //
    m_treeComposite = new SwingScoutTree();
    m_treeComposite.createField(getScoutObject().getTree(), getSwingEnvironment());
    m_treeComposite.getSwingScrollPane().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
    m_treeComposite.getSwingScrollPane().setViewportBorder(new EmptyBorder(0, 4, 0, 4));
    container.add(m_treeComposite.getSwingScrollPane());
    // layout
    setSwingLabel(label);
    setSwingContainer(container);
    setSwingField(m_treeComposite.getSwingTree());
    container.setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  public JScrollPane getSwingScrollPane() {
    return m_treeComposite.getSwingScrollPane();
  }

  public JTreeEx getSwingTree() {
    return m_treeComposite.getSwingTree();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    getSwingScrollPane().getViewport().setOpaque(b);
  }

}
