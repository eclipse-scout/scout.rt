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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
import org.eclipse.scout.rt.platform.classid.ClassId;

public class EntityNode extends AbstractComposerNode {

  private IDataModelEntity m_entity;
  private boolean m_negated = false;
  private List<Object> m_values;
  private List<String> m_texts;

  public EntityNode(IComposerField composerField, IDataModelEntity entity) {
    super(composerField, false);
    m_entity = entity;
    callInitializer();
  }

  @Override
  protected void execInitTreeNode() {
    List<IMenu> menus = new ArrayList<IMenu>();
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
    setMenus(menus);
  }

  @Override
  protected void execDecorateCell(Cell cell) {
    StringBuilder label = new StringBuilder();
    if (getSiblingBefore() != null) {
      label.append(ScoutTexts.get("ExtendedSearchAnd") + " ");
    }
    if (isNegative()) {
      label.append(ScoutTexts.get("ExtendedSearchNot") + " ");
    }
    label.append(m_entity.getText());
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

  public List<Object> getValues() {
    return CollectionUtility.arrayList(m_values);
  }

  public void setValues(List<? extends Object> values) {
    m_values = CollectionUtility.arrayListWithoutNullElements(values);
  }

  public List<String> getTexts() {
    return CollectionUtility.arrayList(m_texts);
  }

  public void setTexts(List<String> texts) {
    m_texts = CollectionUtility.arrayListWithoutNullElements(texts);
  }

  @Order(10)
  @ClassId("8d03343b-7dd8-4d88-8686-9b835fe4dd6d")
  public class AddAttributeOnEntityMenu extends AbstractAddAttributeMenu {
    public AddAttributeOnEntityMenu() {
      super(getComposerField(), EntityNode.this);
    }
  }

  @Order(20)
  @ClassId("75ef4b93-4c3b-463f-b221-a786eb6cbdf8")
  public class AddEitherOrOnEntityMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchAddEitherOrMenu");
    }

    @Override
    protected void execInitAction() {
      List<IDataModelAttribute> atts = m_entity.getAttributes();
      List<IDataModelEntity> ents = m_entity.getEntities();
      setVisible(CollectionUtility.hasElements(atts) || CollectionUtility.hasElements(ents));
    }

    @Override
    protected void execAction() {
      ITreeNode node = getComposerField().addEitherNode(EntityNode.this, false);
      getComposerField().addAdditionalOrNode(node, false);
    }
  }

  @Order(40)
  @ClassId("fa6a5b03-6110-4026-a9c0-8e453c39ba2a")
  public class NegateEntityMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchNegateMenu");
    }

    @Override
    protected void execAction() {
      setNegative(!isNegative());
      if (!isStatusInserted()) {
        setStatusInternal(ITreeNode.STATUS_UPDATED);
      }
      update();
    }
  }

  @Order(50)
  @ClassId("f9fc783d-341a-48de-a3a9-9ee7ab1a51ba")
  public class DeleteEntityMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchRemoveMenu");
    }

    @Override
    protected String getConfiguredKeyStroke() {
      return "delete";
    }

    @Override
    protected void execAction() {
      getTree().selectPreviousParentNode();
      getTree().removeNode(EntityNode.this);
    }
  }

  @Order(60)
  public class Separator2Menu extends AbstractMenuSeparator {
  }

  @Order(70)
  public class AddEntityPlaceholderOnEntityMenu extends AbstractMenuSeparator {
  }

}
