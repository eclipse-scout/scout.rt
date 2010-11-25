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
import org.eclipse.scout.rt.client.ui.form.fields.composer.entity.IComposerEntity;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Dynamic menu to add a new entity to the composer tree
 */
public class AddEntityMenu extends AbstractMenu {
  private final IComposerField m_field;
  private final ITreeNode m_parentNode;
  private final IComposerEntity m_entity;

  public AddEntityMenu(IComposerField field, ITreeNode parentNode, IComposerEntity e) {
    super(false);
    m_field = field;
    m_parentNode = parentNode;
    m_entity = e;
    callInitializer();
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    setText(ScoutTexts.get("ExtendedSearchAddEntityPrefix") + " " + m_entity.getText());
    setIconId(m_entity.getIconId());
  }

  @Override
  protected void execPrepareAction() {
    setVisible(m_entity.isVisible());
  }

  @Override
  protected void execAction() throws ProcessingException {
    m_field.addEntityNode(m_parentNode, m_entity, false, null, null);
  }

}
