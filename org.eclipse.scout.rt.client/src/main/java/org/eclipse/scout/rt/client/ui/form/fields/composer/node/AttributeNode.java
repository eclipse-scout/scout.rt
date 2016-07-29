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
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.platform.classid.ClassId;

public class AttributeNode extends AbstractComposerNode {
  private IDataModelAttribute m_attribute;
  private Integer m_aggregationType;
  private IDataModelAttributeOp m_op;
  private List<Object> m_values;
  private List<String> m_texts;

  public AttributeNode(IComposerField composerField, IDataModelAttribute attrbiute) {
    super(composerField, false);
    m_attribute = attrbiute;
    callInitializer();
  }

  @Override
  protected void execDecorateCell(Cell cell) {
    // text
    int dataType = DataModelConstants.TYPE_NONE;
    if (m_op != null) {
      dataType = m_op.getType();
    }
    if (dataType == DataModelConstants.TYPE_INHERITED) {
      if (m_attribute != null) {
        dataType = m_attribute.getType();
      }
    }
    String prefix = "";
    if (getSiblingBefore() != null) {
      prefix = ScoutTexts.get("ExtendedSearchAnd") + " ";
    }
    if (m_op != null && m_attribute != null) {
      if (m_attribute.getType() == DataModelConstants.TYPE_AGGREGATE_COUNT) {
        cell.setText(prefix + m_op.createVerboseText(null, m_attribute.getText(), m_texts));
      }
      else {
        cell.setText(prefix + m_op.createVerboseText(getAggregationType(), m_attribute.getText(), m_texts));
      }
    }
    else if (m_attribute != null) {
      cell.setText(prefix + m_attribute.getText());
    }
    else {
      // nop
    }
  }

  /**
   * guaranteed to never return null
   */
  public List<Object> getValues() {
    return CollectionUtility.arrayList(m_values);
  }

  public void setValues(List<? extends Object> values) {
    m_values = CollectionUtility.arrayList(values);
  }

  /**
   * guaranteed to never return null
   */
  public List<String> getTexts() {
    return CollectionUtility.arrayList(m_texts);
  }

  public void setTexts(List<String> s) {
    m_texts = CollectionUtility.arrayListWithoutNullElements(s);
  }

  public IDataModelAttribute getAttribute() {
    return m_attribute;
  }

  public void setAttribute(IDataModelAttribute a) {
    m_attribute = a;
  }

  public IDataModelAttributeOp getOp() {
    return m_op;
  }

  public void setOp(IDataModelAttributeOp op) {
    m_op = op;
  }

  /**
   * @return the operator type see {@link ComposerConstants#AGGREGATION_*} values
   */
  public Integer getAggregationType() {
    return m_aggregationType;
  }

  /**
   * @return the operator type see {@link ComposerConstants#AGGREGATION_*} values
   */
  public void setAggregationType(Integer a) {
    if (a != null && a == DataModelConstants.AGGREGATION_NONE) {
      a = null;
    }
    m_aggregationType = a;
  }

  @Order(1)
  @ClassId("fb5120e5-26be-426d-a589-7e88a5c577f0")
  public class EditAttributeMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchEditAttributeMenu");
    }

    @Override
    protected void execAction() {
      ComposerAttributeForm form = new ComposerAttributeForm();
      ITreeNode parentEntity = findParentNode(EntityNode.class);
      if (parentEntity != null) {
        form.setAvailableAttributes(((EntityNode) parentEntity).getEntity().getAttributes());
      }
      else {
        form.setAvailableAttributes(getComposerField().getAttributes());
      }
      form.setSelectedAttribute(getAttribute());
      form.setSelectedOp(getOp());
      form.setSelectedValues(getValues());
      form.setSelectedDisplayValues(getTexts());
      form.startModify();
      form.waitFor();
      if (form.isFormStored()) {
        setAttribute(form.getSelectedAttribute());
        setOp(form.getSelectedOp());
        setValues(form.getSelectedValues());
        setTexts(form.getSelectedDisplayValues());
        if (!isStatusInserted()) {
          setStatusInternal(ITreeNode.STATUS_UPDATED);
        }
        update();
      }
    }
  }

  @Order(3)
  @ClassId("24139c45-1e06-4967-a27f-7d4c457d9b30")
  public class DeleteAttributeMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchRemoveAttributeMenu");
    }

    @Override
    protected String getConfiguredKeyStroke() {
      return "delete";
    }

    @Override
    protected void execAction() {
      getTree().selectPreviousParentNode();
      getTree().removeNode(AttributeNode.this);
    }
  }

}
