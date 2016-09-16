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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * Dynamic menu to add a new entity to the composer tree
 */
@ClassId("08771479-8a4b-42de-bf8c-521215cc214e")
@SuppressWarnings("bsiRulesDefinition:orderMissing")
public class AddEntityMenu extends AbstractMenu {
  private final IComposerField m_field;
  private final ITreeNode m_parentNode;
  private final IDataModelEntity m_entity;
  private PropertyChangeListener m_propertyChangeListener;

  public AddEntityMenu(IComposerField field, ITreeNode parentNode, IDataModelEntity e) {
    super(false);
    m_field = field;
    m_parentNode = parentNode;
    m_entity = e;
    callInitializer();
  }

  @Override
  protected void execInitAction() {
    setText(ScoutTexts.get("ExtendedSearchAddEntityPrefix") + " " + m_entity.getText());
    setIconId(m_entity.getIconId());
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      m_entity.addPropertyChangeListener(m_propertyChangeListener);
    }
    updateVisibility();
  }

  @Override
  public void disposeInternal() {
    super.disposeInternal();
    if (m_propertyChangeListener != null) {
      m_entity.removePropertyChangeListener(m_propertyChangeListener);
      m_propertyChangeListener = null;
    }
  }

  private void updateVisibility() {
    setVisible(m_entity.isVisible());
  }

  @Override
  protected void execAction() {
    m_field.addEntityNode(m_parentNode, m_entity, false, null, null);
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IDataModelEntity.PROP_VISIBLE.equals(evt.getPropertyName())) {
        updateVisibility();
      }
    }
  }
}
