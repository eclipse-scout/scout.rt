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
package org.eclipse.scout.rt.client.ui.form.fields.composer.node;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;

public abstract class AbstractAddAttributeMenu extends AbstractMenu {
  private final IComposerField m_field;
  private final ITreeNode m_parentNode;

  public AbstractAddAttributeMenu(IComposerField field, ITreeNode parentNode) {
    super(false);
    m_field = field;
    m_parentNode = parentNode;
    callInitializer();
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("ExtendedSearchAddAttributeMenu");
  }

  @Override
  protected void execPrepareAction() throws ProcessingException {
    EntityNode eNode = null;
    ITreeNode n = m_parentNode;
    while (n != null) {
      if (n instanceof EntityNode) {
        eNode = (EntityNode) n;
        break;
      }
      n = n.getParentNode();
    }
    IDataModelAttribute[] atts;
    if (eNode != null) {
      atts = eNode.getEntity().getAttributes();
    }
    else {
      atts = m_field.getAttributes();
    }
    setVisible(atts.length > 0);
  }

  @Override
  protected void execAction() throws ProcessingException {
    ComposerAttributeForm form = new ComposerAttributeForm();
    EntityNode eNode = null;
    ITreeNode n = m_parentNode;
    while (n != null) {
      if (n instanceof EntityNode) {
        eNode = (EntityNode) n;
        break;
      }
      n = n.getParentNode();
    }
    if (eNode != null) {
      form.setAvailableAttributes(eNode.getEntity().getAttributes());
    }
    else {
      form.setAvailableAttributes(m_field.getAttributes());
    }
    form.startNew();
    form.waitFor();
    if (form.isFormStored()) {
      IDataModelAttribute a = form.getSelectedAttribute();
      IDataModelAttributeOp op = form.getSelectedOp();
      Object[] values = form.getSelectedValues();
      String[] displayValues = form.getSelectedDisplayValues();
      m_field.addAttributeNode(m_parentNode, a, null, op, values, displayValues);
    }
  }

}
