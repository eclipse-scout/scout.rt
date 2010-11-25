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
package org.eclipse.scout.rt.shared.data.form.fields.composer;

import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;

/**
 * Data representation for a composer attribute value instance in a {@link AbstractComposerData}
 */
public class ComposerAttributeNodeData extends TreeNodeData implements ComposerConstants {
  private static final long serialVersionUID = 1L;

  private AbstractComposerAttributeData m_attribute;
  private int m_operator;
  private Integer m_aggregationType;
  private boolean m_negated = false;

  public ComposerAttributeNodeData() {
  }

  public AbstractComposerAttributeData getAttribute() {
    return m_attribute;
  }

  public void setAttribute(AbstractComposerAttributeData attribute) {
    m_attribute = attribute;
  }

  /**
   * any of the {@link ComposerConstants}.OPERATOR_* values
   */
  public int getOperator() {
    return m_operator;
  }

  /**
   * any of the {@link ComposerConstants}.OPERATOR_* values
   */
  public void setOperator(int operation) {
    m_operator = operation;
  }

  /**
   * any of the {@link ComposerConstants}.AGGREGATION_* values
   */
  public Integer getAggregationType() {
    return m_aggregationType;
  }

  /**
   * any of the {@link ComposerConstants}.AGGREGATION_* values
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
