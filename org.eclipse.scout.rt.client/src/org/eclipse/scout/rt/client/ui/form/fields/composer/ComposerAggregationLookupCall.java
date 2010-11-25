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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerConstants;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Convenience lookup call to present {@link IComposerAttribute#getAggregationTypes()}
 * <p>
 * This lookup call expects the property {@link #setComposerAttribute(IComposerAttribute)} to be set.
 */
public class ComposerAggregationLookupCall extends LocalLookupCall {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ComposerAggregationLookupCall.class);

  private IComposerAttribute m_attribute;
  private List<LookupRow> m_lookupRows;

  public void setComposerAttribute(IComposerAttribute attribute) {
    if (m_attribute != attribute) {
      m_attribute = attribute;
      try {
        updateLookupRows();
      }
      catch (Throwable t) {
        LOG.error("Failed updating aggregation lookup rows for attribute " + attribute);
        m_lookupRows = new ArrayList<LookupRow>();
      }
    }
  }

  public IComposerAttribute getComposerAttribute() {
    return m_attribute;
  }

  protected void updateLookupRows() throws ProcessingException {
    List<LookupRow> result = new ArrayList<LookupRow>();
    int[] ags = null;
    if (m_attribute != null) {
      if (m_attribute.getType() != IComposerAttribute.TYPE_AGGREGATE_COUNT) {
        //add default entry
        result.add(new LookupRow(ComposerConstants.AGGREGATION_NONE, m_attribute.getText()));
      }
      //add valid entries
      ags = m_attribute.getAggregationTypes();
      if (ags != null && ags.length > 0) {
        for (int ag : ags) {
          String text = null;
          switch (ag) {
            case ComposerConstants.AGGREGATION_AVG: {
              text = ScoutTexts.get("ComposerFieldAggregationAvg", m_attribute.getText());
              break;
            }
            case ComposerConstants.AGGREGATION_COUNT: {
              if (m_attribute.getType() == IComposerAttribute.TYPE_AGGREGATE_COUNT) {
                text = m_attribute.getText();
              }
              else {
                text = ScoutTexts.get("ComposerFieldAggregationCount", m_attribute.getText());
              }
              break;
            }
            case ComposerConstants.AGGREGATION_MAX: {
              text = ScoutTexts.get("ComposerFieldAggregationMax", m_attribute.getText());
              break;
            }
            case ComposerConstants.AGGREGATION_MEDIAN: {
              text = ScoutTexts.get("ComposerFieldAggregationMedian", m_attribute.getText());
              break;
            }
            case ComposerConstants.AGGREGATION_MIN: {
              text = ScoutTexts.get("ComposerFieldAggregationMin", m_attribute.getText());
              break;
            }
            case ComposerConstants.AGGREGATION_SUM: {
              text = ScoutTexts.get("ComposerFieldAggregationSum", m_attribute.getText());
              break;
            }
          }
          result.add(new LookupRow(ag, text));
        }
      }
    }
    m_lookupRows = result;
  }

  /**
   * @return the life list of lookup rows that were created for the current attribute
   *         Changed whenever {@link #setComposerAttribute(IComposerAttribute)} is called with another attribute by
   *         calling {@link #updateLookupRows()}.
   */
  public List<LookupRow> getLookupRows() {
    return m_lookupRows;
  }

  @Override
  protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
    if (m_lookupRows != null) {
      return m_lookupRows;
    }
    else {
      return Collections.emptyList();
    }
  }
}
