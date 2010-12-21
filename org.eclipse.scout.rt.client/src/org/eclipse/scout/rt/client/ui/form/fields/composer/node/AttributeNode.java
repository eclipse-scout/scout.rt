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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;

public class AttributeNode extends AbstractComposerNode {
  private IDataModelAttribute m_attribute;
  private Integer m_aggregationType;
  private IDataModelAttributeOp m_op;
  private String m_verbose;
  private Object[] m_values;
  private String[] m_texts;

  public AttributeNode(IComposerField composerField) {
    super(composerField, false);
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
  public Object[] getValues() {
    return m_values;
  }

  public void setValues(Object[] a) {
    m_values = a;
  }

  /**
   * guaranteed to never return null
   */
  public String[] getTexts() {
    return m_texts;
  }

  public void setTexts(String[] s) {
    m_texts = s;
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
   * @return the operator type
   *         see {@link ComposerConstants#AGGREGATION_*} values
   */
  public Integer getAggregationType() {
    return m_aggregationType;
  }

  /**
   * @return the operator type
   *         see {@link ComposerConstants#AGGREGATION_*} values
   */
  public void setAggregationType(Integer a) {
    if (a != null && a == DataModelConstants.AGGREGATION_NONE) {
      a = null;
    }
    m_aggregationType = a;
  }

  @Order(1)
  public class EditAttributeMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchEditAttributeMenu");
    }

    @Override
    protected void execAction() throws ProcessingException {
      ComposerAttributeForm form = new ComposerAttributeForm();
      ITreeNode parent = getParentNode();
      if (parent instanceof EntityNode) {
        form.setAvailableAttributes(((EntityNode) parent).getEntity().getAttributes());
      }
      else {
        form.setAvailableAttributes(getComposerField().getAttributes());
      }
      form.setSelectedAttribute(getAttribute());
      form.setSelectedOp(getOp());
      form.setSelectedValue(getValues() != null && getValues().length > 0 ? getValues()[0] : null);
      form.setSelectedDisplayValue(getTexts() != null && getTexts().length > 0 ? getTexts()[0] : null);
      form.startModify();
      form.waitFor();
      if (form.isFormStored()) {
        setAttribute(form.getSelectedAttribute());
        setOp(form.getSelectedOp());
        setValues(new Object[]{form.getSelectedValue()});
        setTexts(new String[]{form.getSelectedDisplayValue()});
        if (!isStatusInserted()) {
          setStatusInternal(ITreeNode.STATUS_UPDATED);
        }
        update();
      }
    }
  }

  @Order(3)
  public class DeleteAttributeMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchRemoveAttributeMenu");
    }

    @Override
    protected void execAction() throws ProcessingException {
      getTree().removeNode(AttributeNode.this);
    }
  }

}
