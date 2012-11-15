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
package org.eclipse.scout.rt.shared.services.common.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Data model container for a server side {@link IWorkflowService}. <br>
 * By default all inner types of type {@link AbstractWorkflowStepData} of this
 * class are added as the initial set of workflow state datas.
 */
public abstract class AbstractWorkflowData implements Serializable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractWorkflowData.class);
  private static final long serialVersionUID = 1L;

  /*
   * The following fields are used by the workflow framework
   */
  private String m_definitionText;
  private boolean m_definitionActive;
  private String m_definitionServiceClass;
  private Date m_finishDate;
  private List<AbstractWorkflowStepData> m_stepList;
  private int m_currentStepIndex;

  /*
   * The following fields are not diretcly used by the workflow framework
   */
  private long m_definitionNr;
  private long m_workflowNr;
  private int m_statusUid;
  private Date m_creationDate;
  private long m_creationUserNr;
  private String m_comment;

  public AbstractWorkflowData() {
    m_definitionActive = true;
    m_stepList = new ArrayList<AbstractWorkflowStepData>(0);
    m_currentStepIndex = 0;
    initConfig();
  }

  private Class<? extends AbstractWorkflowStepData>[] getConfiguredStepDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, AbstractWorkflowStepData.class);
  }

  protected void initConfig() {
    Class<? extends AbstractWorkflowStepData>[] stepArray = getConfiguredStepDatas();
    for (int i = 0; i < stepArray.length; i++) {
      AbstractWorkflowStepData f;
      try {
        f = ConfigurationUtility.newInnerInstance(this, stepArray[i]);
        m_stepList.add(f);
      }// end try
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }// end for
  }

  /*
   * definiton
   */

  public long getDefinitionNr() {
    return m_definitionNr;
  }

  public void setDefinitionNr(long l) {
    m_definitionNr = l;
  }

  public String getDefinitionText() {
    return m_definitionText;
  }

  public void setDefinitionText(String s) {
    m_definitionText = s;
  }

  public boolean isDefinitionActive() {
    return m_definitionActive;
  }

  public void setDefinitionActive(boolean b) {
    m_definitionActive = b;
  }

  public String getDefinitionServiceClass() {
    return m_definitionServiceClass;
  }

  public void setDefinitionServiceClass(String s) {
    m_definitionServiceClass = s;
  }

  /*
   * instance
   */

  public long getWorkflowNr() {
    return m_workflowNr;
  }

  public void setWorkflowNr(long l) {
    m_workflowNr = l;
  }

  public Date getCreationDate() {
    return m_creationDate;
  }

  public void setCreationDate(Date d) {
    m_creationDate = d;
  }

  public long getCreationUserNr() {
    return m_creationUserNr;
  }

  public void setCreationUserNr(long l) {
    m_creationUserNr = l;
  }

  public int getStatusUid() {
    return m_statusUid;
  }

  public void setStatusUid(int l) {
    m_statusUid = l;
  }

  public String getComment() {
    return m_comment;
  }

  public void setComment(String s) {
    m_comment = s;
  }

  public boolean isFinished() {
    return m_finishDate != null;
  }

  public Date getFinishDate() {
    return m_finishDate;
  }

  public void setFinishDate(Date d) {
    m_finishDate = d;
  }

  /**
   * add an additional step data to this data model
   */
  public void addStepData(AbstractWorkflowStepData d) {
    if (d != null) {
      m_stepList.add(d);
    }
  }

  public AbstractWorkflowStepData[] getStepDataList() {
    AbstractWorkflowStepData[] copyUnmodifiableList = CollectionUtility.toArray(m_stepList, AbstractWorkflowStepData.class);
    return copyUnmodifiableList;
  }

  public void setStepDataList(Collection<AbstractWorkflowStepData> c) {
    if (c != null && c.size() > 0) {
      m_stepList = CollectionUtility.copyList(c);
    }
    else {
      m_stepList.clear();
    }
  }

  /**
   * @return first found step data of desired type Note that the data model
   *         might contain multiple instances of the same type
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractWorkflowStepData> T findStepData(Class<T> workflowStepClass) {
    for (AbstractWorkflowStepData step : m_stepList) {
      if (step.getClass().equals(workflowStepClass)) {
        return (T) step;
      }
    }
    return null;
  }

  public AbstractWorkflowStepData findStepData(Long stepDefinitionNr) {
    if (stepDefinitionNr == null) {
      return null;
    }
    for (AbstractWorkflowStepData step : m_stepList) {
      if (step.getDefinitionNr() == stepDefinitionNr) {
        return step;
      }
    }
    return null;
  }

  public int findStepDataIndex(AbstractWorkflowStepData stepData) {
    if (stepData != null) {
      for (int i = 0; i < m_stepList.size(); i++) {
        if (m_stepList.get(i) == stepData) {
          return i;
        }
      }
    }
    return -1;
  }

  public int findStepDataIndex(Long stepDefinitionNr) {
    if (stepDefinitionNr != null) {
      for (int i = 0; i < m_stepList.size(); i++) {
        if (m_stepList.get(i).getDefinitionNr() == stepDefinitionNr) {
          return i;
        }
      }
    }
    return -1;
  }

  public int getCurrentStepDataIndex() {
    return m_currentStepIndex;
  }

  public void setCurrentStepData(AbstractWorkflowStepData stepData) {
    setCurrentStepDataIndex(findStepDataIndex(stepData));
  }

  public void setCurrentStepDataIndex(int index) {
    if (index < 0) {
      index = 0;
    }
    if (index >= m_stepList.size()) {
      index = m_stepList.size();
    }
    m_currentStepIndex = index;
  }

  /**
   * advance the step index to the next step in the step list (working steps)
   */
  public void advanceStepDataIndex() {
    m_currentStepIndex++;
  }

  /**
   * @return the step at the current step index
   */
  public AbstractWorkflowStepData getCurrentStepData() {
    if (m_currentStepIndex < m_stepList.size()) {
      return m_stepList.get(m_currentStepIndex);
    }
    else {
      return null;
    }
  }

  /**
   * @return all steps after the current step index
   */
  public List<AbstractWorkflowStepData> getExpectedFuture() {
    int a = m_currentStepIndex;
    int b = m_stepList.size() - 1;
    if (a <= b && b < m_stepList.size()) {
      return Collections.unmodifiableList(m_stepList.subList(a, b + 1));
    }
    else {
      return Collections.emptyList();
    }
  }

  /**
   * @return all steps before the current step index
   */
  public List<AbstractWorkflowStepData> getHistory() {
    int a = 0;
    int b = m_currentStepIndex;
    if (a <= b && b < m_stepList.size()) {
      return Collections.unmodifiableList(m_stepList.subList(a, b + 1));
    }
    else {
      return Collections.emptyList();
    }
  }

  public AbstractWorkflowStepData getPreviousStepData() {
    if (m_currentStepIndex - 1 >= 0 && m_currentStepIndex - 1 < m_stepList.size()) {
      return m_stepList.get(m_currentStepIndex - 1);
    }
    else {
      return null;
    }
  }

  public AbstractWorkflowStepData getNextStepData() {
    if (m_currentStepIndex + 1 >= 0 && m_currentStepIndex + 1 < m_stepList.size()) {
      return m_stepList.get(m_currentStepIndex + 1);
    }
    else {
      return null;
    }
  }

  public AbstractWorkflowStepData getFirstStepData() {
    if (m_stepList.size() > 0) {
      return m_stepList.get(0);
    }
    else {
      return null;
    }
  }

  public AbstractWorkflowStepData getLastStepData() {
    if (m_stepList.size() > 0) {
      return m_stepList.get(m_stepList.size() - 1);
    }
    else {
      return null;
    }
  }

}
