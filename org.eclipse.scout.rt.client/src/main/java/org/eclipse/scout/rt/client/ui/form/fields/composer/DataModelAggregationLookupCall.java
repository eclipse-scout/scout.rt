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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience lookup call to present {@link IDataModelAttribute#getAggregationTypes()}
 * <p>
 * This lookup call expects the property {@link #setAttribute(IDataModelAttribute)} to be set.
 */
@ClassId("d7a38deb-3d92-4bf2-853c-d3fda0219b7f")
public class DataModelAggregationLookupCall extends LocalLookupCall<Integer> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(DataModelAggregationLookupCall.class);

  private IDataModelAttribute m_attribute;
  private List<ILookupRow<Integer>> m_lookupRows;

  public void setAttribute(IDataModelAttribute attribute) {
    if (m_attribute != attribute) {
      m_attribute = attribute;
      try {
        updateLookupRows();
      }
      catch (Exception t) { // NOSONAR
        LOG.error("Failed updating aggregation lookup rows for attribute {}", attribute);
        m_lookupRows = CollectionUtility.emptyArrayList();
      }
    }
  }

  public IDataModelAttribute getAttribute() {
    return m_attribute;
  }

  protected void updateLookupRows() {
    List<ILookupRow<Integer>> result = new ArrayList<ILookupRow<Integer>>();
    int[] ags = null;
    if (m_attribute != null) {
      if (m_attribute.getType() != DataModelConstants.TYPE_AGGREGATE_COUNT) {
        //add default entry
        result.add(new LookupRow<Integer>(DataModelConstants.AGGREGATION_NONE, m_attribute.getText()));
      }
      //add valid entries
      ags = m_attribute.getAggregationTypes();
      if (ags != null && ags.length > 0) {
        for (int ag : ags) {
          String text = null;
          switch (ag) {
            case DataModelConstants.AGGREGATION_AVG: {
              text = TEXTS.get("ComposerFieldAggregationAvg", m_attribute.getText());
              break;
            }
            case DataModelConstants.AGGREGATION_COUNT: {
              if (m_attribute.getType() == IDataModelAttribute.TYPE_AGGREGATE_COUNT) {
                text = m_attribute.getText();
              }
              else {
                text = TEXTS.get("ComposerFieldAggregationCount", m_attribute.getText());
              }
              break;
            }
            case DataModelConstants.AGGREGATION_MAX: {
              text = TEXTS.get("ComposerFieldAggregationMax", m_attribute.getText());
              break;
            }
            case DataModelConstants.AGGREGATION_MEDIAN: {
              text = TEXTS.get("ComposerFieldAggregationMedian", m_attribute.getText());
              break;
            }
            case DataModelConstants.AGGREGATION_MIN: {
              text = TEXTS.get("ComposerFieldAggregationMin", m_attribute.getText());
              break;
            }
            case DataModelConstants.AGGREGATION_SUM: {
              text = TEXTS.get("ComposerFieldAggregationSum", m_attribute.getText());
              break;
            }
          }
          result.add(new LookupRow<Integer>(ag, text));
        }
      }
    }
    m_lookupRows = result;
  }

  /**
   * @return the life list of lookup rows that were created for the current attribute Changed whenever
   *         {@link #setAttribute(IDataModelAttribute)} is called with another attribute by calling
   *         {@link #updateLookupRows()}.
   */
  public List<ILookupRow<Integer>> getLookupRows() {
    return m_lookupRows;
  }

  @Override
  protected List<ILookupRow<Integer>> execCreateLookupRows() {
    if (m_lookupRows != null) {
      return m_lookupRows;
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_attribute == null) ? 0 : m_attribute.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DataModelAggregationLookupCall other = (DataModelAggregationLookupCall) obj;
    if (m_attribute == null) {
      if (other.m_attribute != null) {
        return false;
      }
    }
    else if (!m_attribute.equals(other.m_attribute)) {
      return false;
    }
    return true;
  }
}
