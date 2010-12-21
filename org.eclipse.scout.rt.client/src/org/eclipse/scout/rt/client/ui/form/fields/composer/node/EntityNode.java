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

import java.util.ArrayList;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

public class EntityNode extends AbstractComposerNode {

  private IDataModelEntity m_entity;
  private boolean m_negated = false;
  private Object[] m_values;
  private String[] m_texts;

  public EntityNode(IComposerField composerField) {
    super(composerField, true);
  }

  @Override
  protected void execInitTreeNode() {
    ArrayList<IMenu> menus = new ArrayList<IMenu>();
    for (IMenu m : getMenus()) {
      if (m.getClass() == AddEntityPlaceholderOnEntityMenu.class) {
        attachAddEntityMenus(menus);
      }
      else {
        menus.add(m);
      }
    }
    // delete separator if it is at the end
    if (menus.size() > 0 && menus.get(menus.size() - 1).getClass() == Separator2Menu.class) {
      menus.remove(menus.size() - 1);
    }
    setMenus(menus.toArray(new IMenu[menus.size()]));
  }

  @Override
  protected void execDecorateCell(Cell cell) {
    StringBuffer label = new StringBuffer();
    if (getSiblingBefore() != null) {
      label.append(ScoutTexts.get("ExtendedSearchAnd") + " ");
    }
    if (isNegative()) {
      label.append(ScoutTexts.get("ExtendedSearchNot"));
    }
    label.append(" " + m_entity.getText());
    if (getChildNodeCount() > 0) {
      label.append(" " + ScoutTexts.get("ExtendedSearchEntitySuffix"));
    }
    cell.setText(label.toString());
  }

  public IDataModelEntity getEntity() {
    return m_entity;
  }

  public void setEntity(IDataModelEntity e) {
    m_entity = e;
  }

  public boolean isNegative() {
    return m_negated;
  }

  public void setNegative(boolean b) {
    m_negated = b;
  }

  public Object[] getValues() {
    return m_values;
  }

  public void setValues(Object[] values) {
    m_values = values;
  }

  public String[] getTexts() {
    return m_texts;
  }

  public void setTexts(String[] a) {
    m_texts = a;
  }

  @Order(10)
  public class AddAttributeOnEntityMenu extends AbstractAddAttributeMenu {
    public AddAttributeOnEntityMenu() {
      super(getComposerField(), EntityNode.this);
    }
  }

  @Order(20)
  public class AddEitherOrOnEntityMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchAddEitherOrMenu");
    }

    @Override
    protected void execPrepareAction() throws ProcessingException {
      IDataModelAttribute[] atts = m_entity.getAttributes();
      IDataModelEntity[] ents = m_entity.getEntities();
      setVisible((atts != null && atts.length > 0) || (ents != null && ents.length > 0));
    }

    @Override
    protected void execAction() throws ProcessingException {
      ITreeNode node = getComposerField().addEitherNode(EntityNode.this, false);
      getComposerField().addAdditionalOrNode(node, false);
    }
  }

  @Order(40)
  public class NegateEntityMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchNegateMenu");
    }

    @Override
    protected void execAction() throws ProcessingException {
      setNegative(!isNegative());
      if (!isStatusInserted()) {
        setStatusInternal(ITreeNode.STATUS_UPDATED);
      }
      update();
    }
  }

  @Order(50)
  public class DeleteEntityMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchRemoveMenu");
    }

    @Override
    protected void execAction() throws ProcessingException {
      getTree().removeNode(EntityNode.this);
    }
  }

  @Order(60)
  public class Separator2Menu extends MenuSeparator {
  }

  @Order(70)
  public class AddEntityPlaceholderOnEntityMenu extends MenuSeparator {
  }

}
