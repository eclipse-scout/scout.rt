/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.composer.node;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;

@ClassId("db83fd12-1690-4c31-8528-8f115fda496f")
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
  protected void execOwnerValueChanged(Object newOwnerValue) {
    EntityNode entityNode = null;
    ITreeNode treeNode = m_parentNode;
    while (treeNode != null) {
      if (treeNode instanceof EntityNode) {
        entityNode = (EntityNode) treeNode;
        break;
      }
      treeNode = treeNode.getParentNode();
    }
    List<IDataModelAttribute> attributes;
    if (entityNode != null) {
      attributes = entityNode.getEntity().getAttributes();
    }
    else {
      attributes = m_field.getAttributes();
    }
    setVisible(attributes.size() > 0);
  }

  @Override
  protected void execAction() {
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
      List<Object> values = form.getSelectedValues();
      List<String> displayValues = form.getSelectedDisplayValues();
      m_field.addAttributeNode(m_parentNode, a, null, op, values, displayValues);
    }
  }

}
