/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.fields.composer;

import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;

/**
 * Data representation for a composer attribute value instance in a {@link AbstractComposerData}
 */
public class ComposerAttributeNodeData extends TreeNodeData implements DataModelConstants {
  private static final long serialVersionUID = 1L;

  private String m_attributeExternalId;
  private int m_operator;
  private Integer m_aggregationType;
  private boolean m_negated = false;

  public String getAttributeExternalId() {
    return m_attributeExternalId;
  }

  public void setAttributeExternalId(String attributeExternalId) {
    m_attributeExternalId = attributeExternalId;
  }

  /**
   * any of the {@link DataModelConstants}.OPERATOR_* values
   */
  public int getOperator() {
    return m_operator;
  }

  /**
   * any of the {@link DataModelConstants}.OPERATOR_* values
   */
  public void setOperator(int operation) {
    m_operator = operation;
  }

  /**
   * any of the {@link DataModelConstants}.AGGREGATION_* values
   */
  public Integer getAggregationType() {
    return m_aggregationType;
  }

  /**
   * any of the {@link DataModelConstants}.AGGREGATION_* values
   */
  public void setAggregationType(Integer aggregationType) {
    m_aggregationType = aggregationType;
  }

  public boolean isNegative() {
    return m_negated;
  }

  public void setNegative(boolean b) {
    m_negated = b;
  }
}
