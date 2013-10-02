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
package org.eclipse.scout.rt.server.services.common.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowData;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowStepData;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractWorkflowService<T extends AbstractWorkflowData> extends AbstractService implements IWorkflowService<T> {
  private ArrayList<IWorkflowStep> m_stepList;
  private HashMap<Class<? extends IWorkflowStep>, IWorkflowStep> m_stepMap;

  public AbstractWorkflowService() {
    m_stepList = new ArrayList<IWorkflowStep>();
    m_stepMap = new HashMap<Class<? extends IWorkflowStep>, IWorkflowStep>();
    initConfig();
  }

  @Override
  public AbstractWorkflowData[] getAvailableWorkflowTypes(SearchFilter filter) throws ProcessingException {
    AbstractWorkflowData[] a = execCollectAvailableWorkflowTypes(filter);
    if (a == null) {
      a = new AbstractWorkflowData[0];
    }
    return a;
  }

  @Override
  public AbstractWorkflowData[] getFilteredWorkflows(SearchFilter filter) throws ProcessingException {
    AbstractWorkflowData[] a = execCollectFilteredWorkflows(filter);
    if (a == null) {
      a = new AbstractWorkflowData[0];
    }
    return a;
  }

  @Override
  public T create(T spec) throws ProcessingException {
    T data = execNew(spec);
    if (data != null) {
      data = resume(data);
    }
    return data;
  }

  @Override
  public T resume(T spec) throws ProcessingException {
    T data = execLoad(spec);
    if (data != null) {
      data = makeStateTransition(data);
    }
    return data;
  }

  @Override
  public T store(T data) throws ProcessingException {
    if (data != null) {
      execStore(data);
    }
    return data;
  }

  @Override
  public T finish(T data) throws ProcessingException {
    if (data != null) {
      try {
        execFinish(data);
      }
      finally {
        execStore(data);
      }
    }
    return data;
  }

  @Override
  public T discard(T data) throws ProcessingException {
    if (data != null) {
      try {
        execDiscard(data);
      }
      finally {
        execStore(data);
      }
    }
    return data;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T makeStateTransition(T data) throws ProcessingException {
    execValidateState(data);
    AbstractWorkflowStepData curStepData = data.getCurrentStepData();
    AbstractWorkflowStepData nextStepData = data.getNextStepData();
    IWorkflowStep curStep = null;
    if (curStepData != null) {
      curStep = execGetStepForData(curStepData);
    }
    IWorkflowStep nextStep = null;
    if (nextStepData != null) {
      nextStep = execGetStepForData(nextStepData);
    }
    //
    if (curStep != null) {
      if (curStepData != null && curStepData.isCompleted()) {
        try {
          curStep.completeStep(data, curStepData, nextStep != null);
          execStore(data);
        }
        catch (ProcessingException e) {
          curStepData.setCompletionDate(null);
          curStep.prepareStep(data, curStepData, nextStep != null);
          throw e;
        }
        if (curStepData.isCompleted()) {
          // go on to next step
          data.advanceStepDataIndex();
          execStore(data);
          curStepData = data.getCurrentStepData();
          nextStepData = data.getNextStepData();
          curStep = null;
          if (curStepData != null) {
            curStep = execGetStepForData(curStepData);
          }
          nextStep = null;
          if (nextStepData != null) {
            nextStep = execGetStepForData(nextStepData);
          }
          // if there is no next step, call finish
          if (curStep != null) {
            curStep.prepareStep(data, curStepData, nextStep != null);
            execStore(data);
          }
          else {
            data = finish(data);
          }
        }
      }
      else {
        curStep.prepareStep(data, curStepData, nextStep != null);
        execStore(data);
      }
    }
    else {
      data = finish(data);
    }
    return data;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S extends IWorkflowStep> S getStepByClass(Class<S> stepClass) {
    if (stepClass == null) {
      return null;
    }
    else {
      return (S) m_stepMap.get(stepClass);
    }
  }

  protected IWorkflowStep execGetStepForData(AbstractWorkflowStepData data) {
    if (data != null) {
      for (IWorkflowStep step : m_stepList) {
        Class<?> processingDataType = TypeCastUtility.getGenericsParameterClass(step.getClass(), AbstractWorkflowStep.class);
        if (processingDataType != null && processingDataType.isInstance(data)) {
          return step;
        }
      }
    }
    return null;
  }

  @Override
  public IWorkflowStep[] getSteps() {
    return m_stepList.toArray(new IWorkflowStep[m_stepList.size()]);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S extends IWorkflowStep> S resolveStep(S step) {
    if (step == null) {
      return null;
    }
    else {
      return (S) m_stepMap.get(step.getClass());
    }
  }

  /**
   * set default properties on AbstractWorkflowData object<br>
   * definitionNr,definitionText,definitionActive,definitionDoc,
   * definitionServiceClass
   */
  @SuppressWarnings("unchecked")
  public void assignDefinitions(AbstractWorkflowData data) {
    if (data != null) {
      data.setDefinitionText(getConfiguredDefinitionText());
      data.setDefinitionActive(getConfiguredDefinitionActive());
      data.setDefinitionServiceClass(getClass().getName());
      for (AbstractWorkflowStepData stepData : data.getStepDataList()) {
        IWorkflowStep step = execGetStepForData(stepData);
        if (step != null) {
          step.assignDefinitions(stepData);
        }
      }
    }
  }

  /*
   * Configuration
   */

  private Class<? extends IWorkflowStep>[] getConfiguredSteps() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IWorkflowStep.class);
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  protected String getConfiguredDefinitionText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredDefinitionActive() {
    return false;
  }

  @ConfigProperty(ConfigProperty.DOC)
  @Order(10)
  protected String getConfiguredDoc() {
    return null;
  }

  @ConfigOperation
  @Order(10)
  protected AbstractWorkflowData[] execCollectAvailableWorkflowTypes(SearchFilter filter) throws ProcessingException {
    return new AbstractWorkflowData[0];
  }

  @ConfigOperation
  @Order(15)
  protected AbstractWorkflowData[] execCollectFilteredWorkflows(SearchFilter filter) throws ProcessingException {
    return new AbstractWorkflowData[0];
  }

  /**
   * Create new data object based on the specification data object doesn't have
   * to be be validated yet
   */
  @ConfigOperation
  @Order(20)
  protected T execNew(T spec) throws ProcessingException {
    return spec;
  }

  /**
   * Load data object from persistent store based on the specification {@link AbstractWorkflowData#getWorkflowNr()} is
   * the primary key
   * <p>
   * This method is called on the creation of a new workflow data and also when a workflow resumes.
   */
  @ConfigOperation
  @Order(30)
  protected T execLoad(T spec) throws ProcessingException {
    return spec;
  }

  /**
   * Make data object persistent<br>
   * This method is called whenever a step has changed, so in most cases it is
   * sufficient to only store the current step data
   */
  @ConfigOperation
  @Order(40)
  protected void execStore(T data) throws ProcessingException {

  }

  /**
   * Recalculates / validates the currently active (not yet completed) step
   * {@link AbstractWorkflowData#setCurrentStep(AbstractWorkflowStepData)},<br>
   * sets the {@link AbstractWorkflowData#setHistory()} and {@link AbstractWorkflowData#setExpectedFuture()} and
   * validates {@link AbstractWorkflowStepData#setFinishPossible(boolean, int)},
   * {@link AbstractWorkflowStepData#setCancelPossible(boolean)},
   * {@link AbstractWorkflowStepData#setSuspendPossible(boolean)},
   * {@link AbstractWorkflowStepData#setCompletionDate(java.util.Date)}
   */
  @ConfigOperation
  @Order(50)
  protected void execValidateState(T data) throws ProcessingException {
    assignDefinitions(data);
  }

  /**
   * Finish workflow based on that data
   */
  @ConfigOperation
  @Order(60)
  protected void execFinish(T data) throws ProcessingException {
    data.setFinishDate(new Date());
  }

  /**
   * Discard workflow based on that data
   */
  @ConfigOperation
  @Order(70)
  protected void execDiscard(T data) throws ProcessingException {

  }

  protected void initConfig() {
    for (Class<? extends IWorkflowStep> c : getConfiguredSteps()) {
      try {
        IWorkflowStep step = ConfigurationUtility.newInnerInstance(this, c);
        m_stepList.add(step);
        m_stepMap.put(c, step);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("create instance of " + c, e));
      }
    }
  }

}
